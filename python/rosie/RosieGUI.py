from tkinter import *
import tkinter.font

import sys
from turtle import bgcolor

from . import RosieClient
from rosie.events import AgentMessageSent, ScriptMessageSent, InstructorMessageSent

class RosieGUI(Frame):
    class MessagesList(Listbox):
        """ Displays a list of messages that are the interaction history """
        def __init__(self, parent):
            Listbox.__init__(self, parent, font=("Times", "24"), takefocus=0)
            self.parent = parent
            self.scrollbar = Scrollbar(self)
            self.config(yscrollcommand=self.scrollbar.set)
            self.scrollbar.config(command=self.yview)
            self.scrollbar.pack(side=RIGHT, fill=Y)

        def add(self, message):
            self.insert(END, message)
            self.see(END)

    class ScriptList(Listbox):
        """ Displays a list of script messages defined for Rosie """
        def __init__(self, parent, script):
            Listbox.__init__(self, parent, font=("Times", "16"), selectmode=SINGLE)
            self.parent = parent

            self.scrollbar = Scrollbar(self)
            self.config(yscrollcommand=self.scrollbar.set)
            self.scrollbar.config(command=self.yview)
            self.scrollbar.pack(side=RIGHT, fill=Y)
            self.bind('<Double-Button-1>', lambda ev: self.on_script_double_click(ev))

            # Add the script messages to the list
            self.script = script
            for message in self.script:
                self.insert(END, message)

        def on_script_double_click(self, event):
            index = self.nearest(event.y)
            message = self.get(index)
            self.parent.send_message_to_rosie(message)

        def select_line(self, index):
            self.selection_clear(0, END)
            self.selection_set(index)
            self.event_generate("<<ListboxSelect>>")
            self.see(index)

    class ChatEntry(Entry):
        """ Defines a text entry box where the instructor can type messages for Rosie
            Also keeps track of the message history and supports Up/Down keys to navigate """
        def __init__(self, parent):
            Entry.__init__(self, parent, font=("Times", "16"), state="normal")
            self.parent = parent

            # self.bind('<Return>', lambda key: self.send_message())
            # self.bind('<Up>', lambda key: self.scroll_history(-1))
            # self.bind('<Down>', lambda key: self.scroll_history(1))

            self.message_history = []
            self.history_index = 0

        def add_to_history(self, message):
            if len(self.message_history) == 0 or self.message_history[-1] != message:
                self.message_history.append(message)
            self.history_index = len(self.message_history)

        def scroll_history(self, delta):
            """ change the index into the message history by delta (integer)
                and set the message entry box to that message """
            self.clear()
            if delta < 0 and self.history_index + delta < 0:
                self.history_index = 0
            elif delta > 0 and self.history_index + delta >= len(self.message_history):
                self.history_index = len(self.message_history)
            else:
                self.history_index += delta

            if self.history_index < len(self.message_history):
                self.insert(END, self.message_history[self.history_index])

        def send_message(self):
            self.parent.send_message_to_rosie(self.get().strip())
            self.clear()

        def clear(self):
            self.delete(0, END)

    #ToDo: We may not need a class for this switch unless we are literally running functions
    class SwitchTextBoxes:
        def __init__(self, parent):
            self.parent = parent

    """ Creates a single chat box interface to send messages to Rosie and receive messages back """
    def __init__(self, rosie_client, master):
        Frame.__init__(self, master, width=1000, height=600)
        self.rosie_client = rosie_client

        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.combined_message = ""
        self.chat_entries = []
        self.chat_entry_labels = []

        self.create_widgets()
        self.setup_rosie_client()

        master.geometry("1200x800")
        master.protocol("WM_DELETE_WINDOW", self.on_exit)

    def setup_rosie_client(self):
        """ Put any code that connects with the rosie_client here """
        self.rosie_client.add_event_handler(AgentMessageSent, lambda e: self.messages_list.add(e.message))
        self.rosie_client.add_event_handler(InstructorMessageSent, lambda e: self.messages_list.add(e.message))
        self.rosie_client.add_event_handler(ScriptMessageSent, lambda e: self.script_list.select_line(e.index))

    def run(self):
        self.rosie_client.connect()
        self.master.mainloop()

    def send_combined_message(self):
    # maybe use the add_to_history function as a way to combine over time.
    ## ToDo - change font sizes back
        self.combined_message = ""
        
        if self.last_selected_option == 0:
            for i in range(2,4):
                self.combined_message += self.chat_entries[0][i-2].get().strip() + " "
        elif self.last_selected_option == 1:
            for i in range(2,6):
                self.combined_message += self.chat_entries[1][i-2].get().strip() + " "
        elif self.last_selected_option == 2:
            self.combined_message = "The goal is that "
            for i in range(2,5):
                self.combined_message += self.chat_entries[2][i-2].get().strip() + " "
        else:
            self.combined_message = "The goal is that "
            for i in range(2,6):
                self.combined_message += self.chat_entries[3][i-2].get().strip() + " "
        
        self.combined_message = self.combined_message.strip() + "."    
        self.send_message_to_rosie(self.combined_message)
        for sublist in self.chat_entries:
            for i in range(len(sublist)):
                sublist[i].clear()

    """ Function to hide all template labels and entries"""
    def hide_template_entries(self):
        for row in self.chat_entries:
            for entry in row:
                entry.grid_remove()
        for row in self.chat_entry_labels:
            for label in row:
                label.grid_remove()

    """ Function to show the template labels and entries linked to the selected option in the options menu"""
    def show_selected_template_entries(self, selected_index):
        for entry in self.chat_entries[selected_index]:
            entry.grid()
        for label in self.chat_entry_labels[selected_index]:
            label.grid()
        
    def create_widgets(self):
        # ToDo: A way to clean up this code may be to get all the grid statements together and function statements(lambda etc) in another batch
        # https://tkdocs.com/tutorial/grid.html
        # https://www.askpython.com/python-modules/tkinter/python-tkinter-grid-example
        self.grid(row=0, column=0, sticky=N+S+E+W)

        # 10 rows x 6 cols grid        
        self.columnconfigure(0, weight=1, minsize=150)
        self.columnconfigure(1, weight=1, minsize=150)
        self.columnconfigure(2, weight=1, minsize=150)
        self.columnconfigure(3, weight=1, minsize=150)
        self.columnconfigure(4, weight=1, minsize=150)
        self.columnconfigure(5, weight=1, minsize=150)
        self.rowconfigure(0, weight=1, minsize=25)
        self.rowconfigure(1, weight=20, minsize=300)
        self.rowconfigure(2, weight=1, minsize=30)

        # Action Templates 1 and 2
        self.rowconfigure(3, weight=2, minsize=30)
        self.rowconfigure(4, weight=2, minsize=30)
        self.rowconfigure(5, weight=2, minsize=30)

        # Goal Templates 1 and 2
        self.rowconfigure(6, weight=2, minsize=30)
        self.rowconfigure(7, weight=2, minsize=30)
        self.rowconfigure(8, weight=2, minsize=30)

        self.rowconfigure(9, weight=1, minsize=30)


        # Row 0: Top row of buttons for running Rosie
        self.run_button = Button(self, text="Run", font=("Times", "18"))
        self.run_button["command"] = lambda: self.rosie_client.start()
        self.run_button.grid(row=0, column=0, columnspan=2, sticky=N+S+E+W)

        self.stop_button = Button(self, text="Stop", font=("Times", "18"))
        self.stop_button["command"] = lambda: self.rosie_client.stop()
        self.stop_button.grid(row=0, column=2, columnspan=2, sticky=N+S+E+W)


        # Row 1: Message History (Col 0-4)
        self.messages_list = RosieGUI.MessagesList(self)
        self.messages_list.grid(row=1, column=0, columnspan=7, sticky=N+S+E+W)

        # Old Row 1: Script Messages (Col 5-7)
        # print("AGENT MESSAGES")
        # print(self.rosie_client.messages)
        # self.script_list = RosieGUI.ScriptList(self, self.rosie_client.messages)
        # self.script_list.click_handler = lambda msg: self.send_message_to_rosie(msg)
        # self.script_list.grid(row=1, column=4, columnspan=4, sticky=N+S+E+W)


        # Row 2: OptionMenu dropbox to select different template entry boxes (Col 0-2)
        self.last_selected_option = -1
        
        def callback(*args):            
            # Clear the textboxes from the last selected option
            if self.last_selected_option != -1:
                for entry in self.chat_entries[self.last_selected_option]:
                    entry.grid_remove()
                for label in self.chat_entry_labels[self.last_selected_option]:
                    label.grid_remove()

            selected_index = options.index(selected_option.get().strip())
            self.last_selected_option = selected_index
            self.show_selected_template_entries(selected_index)
    
        #Set the Menu initially
        selected_option = StringVar(value="Select Any Template Input")
        selected_option.trace("w", callback)
    
        options = ["Action Template 1", 
                   "Action Template 2", 
                   "Goal Template 1", 
                   "Goal Template 2"]
      
        self.template_optionsmenu = OptionMenu(self, selected_option, *options)
        self.template_optionsmenu.grid(row=2, column=0, columnspan=2, sticky=N+S+E+W)
        self.template_optionsmenu.config(font=("Arial", "14"))
        self.template_optionsmenu["menu"].config(font=("Arial", "14"))


        # Row 3-8: Action and Goal Template Labels and Text Entries 

        # Row 3-4: Action Template 1 Labels (Col 0-1)
        action_template1_label_list = []
        label1_et1 = Label(self, text="Verb", font=("Times", "12"))
        label1_et1.grid(row=3, column=0, columnspan=1)
        label1_et2 = Label(self, text="Turn on", font=("Times", "14", "italic"))
        label1_et2.grid(row=4, column=0, columnspan=1)
        label1_et3 = Label(self, text="Noun Phrase", font=("Times", "12"))
        label1_et3.grid(row=3, column=1, columnspan=1)
        label1_et4 = Label(self, text="the alarm", font=("Times", "14", "italic"))
        label1_et4.grid(row=4, column=1, columnspan=1)
        action_template1_label_list.extend([label1_et1,label1_et2,label1_et3,label1_et4])
        self.chat_entry_labels.append(action_template1_label_list)

        # Row 5: Action Template 1 Text Input (Col 0-1)
        action_template1_list = []
        for i in range(0,2):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=5, column=i, columnspan=1, sticky=N+S+E+W)
            action_template1_list.append(entry)
        self.chat_entries.append(action_template1_list)
        
        # Row 3-4: Action Template 2 Labels (Col 0-3)
        action_template2_label_list = []
        label2_et1 = Label(self, text="Verb", font=("Times", "12"))
        label2_et1.grid(row=3, column=0, columnspan=1)
        label2_et2 = Label(self, text="Fetch", font=("Times", "14", "italic"))
        label2_et2.grid(row=4, column=0, columnspan=1)
        label2_et3 = Label(self, text="Noun Phrase", font=("Times", "12"))
        label2_et3.grid(row=3, column=1, columnspan=1)
        label2_et4 = Label(self, text="an extinguisher", font=("Times", "14", "italic"))
        label2_et4.grid(row=4, column=1, columnspan=1)
        label2_et5 = Label(self, text="Preposition", font=("Times", "12"))
        label2_et5.grid(row=3, column=2, columnspan=1)
        label2_et6 = Label(self, text="from", font=("Times", "14", "italic"))
        label2_et6.grid(row=4, column=2, columnspan=1)
        label2_et7 = Label(self, text="Noun Phrase", font=("Times", "12"))
        label2_et7.grid(row=3, column=3, columnspan=1)
        label2_et8 = Label(self, text="the supply room", font=("Times", "14", "italic"))
        label2_et8.grid(row=4, column=3, columnspan=1)
        action_template2_label_list.extend([label2_et1,label2_et2,label2_et3,label2_et4,label2_et5,label2_et6,label2_et7,label2_et8])
        self.chat_entry_labels.append(action_template2_label_list)
        
        # Row 5: Action Template 2 Text Input (Col 0-3)
        action_template2_list = []
        for i in range(0,4):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=5, column=i, columnspan=1, sticky=N+S+E+W)
            action_template2_list.append(entry)        
        self.chat_entries.append(action_template2_list)

        # Row 6-8: Goal Template 1
        goal_template1_label_list = []
        # Goal Template 1 - Chat Entry Default Label
        goal_label1_et1 = Label(self, text="The goal is that", font=("Times", "14"))
        goal_label1_et1.grid(row=8, column=0, columnspan=1)
        # Goal Template 1 Labels (Col 1-3)
        goal_label1_et2 = Label(self, text="Noun Phrase", font=("Times", "12"))
        goal_label1_et2.grid(row=6, column=1, columnspan=1)
        goal_label1_et3 = Label(self, text="the alarm", font=("Times", "14", "italic"))
        goal_label1_et3.grid(row=7, column=1, columnspan=1)
        goal_label1_et4 = Label(self, text="Verb", font=("Times", "12"))
        goal_label1_et4.grid(row=6, column=2, columnspan=1)
        goal_label1_et5 = Label(self, text="is", font=("Times", "14", "italic"))
        goal_label1_et5.grid(row=7, column=2, columnspan=1)
        goal_label1_et6 = Label(self, text="Object State", font=("Times", "12"))
        goal_label1_et6.grid(row=6, column=3, columnspan=1)
        goal_label1_et7 = Label(self, text="on", font=("Times", "14", "italic"))
        goal_label1_et7.grid(row=7, column=3, columnspan=1)
        
        goal_template1_label_list.extend([goal_label1_et1,goal_label1_et2,goal_label1_et3,goal_label1_et4,goal_label1_et5,goal_label1_et6,goal_label1_et7])
        self.chat_entry_labels.append(goal_template1_label_list)

        # Row 8: Goal Template 1 Text Input (Col 1-3)
        goal_template1_list = []
        for i in range(1,4):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=8, column=i, columnspan=1, sticky=N+S+E+W)
            goal_template1_list.append(entry)
        self.chat_entries.append(goal_template1_list)  

        # Row 6-8: Goal Template 2
        goal_template2_label_list = []
        # Goal Template 2 - Chat Entry Default Label
        goal_label2_et1 = Label(self, text="The goal is that", font=("Times", "14"))
        goal_label2_et1.grid(row=8, column=0, columnspan=1)
        # Goal Template 2 Labels (Col 1-3)
        goal_label2_et2 = Label(self, text="Noun Phrase", font=("Times", "12"))
        goal_label2_et2.grid(row=6, column=1, columnspan=1)
        goal_label2_et3 = Label(self, text="the extinguisher", font=("Times", "14", "italic"))
        goal_label2_et3.grid(row=7, column=1, columnspan=1)
        goal_label2_et4 = Label(self, text="Verb", font=("Times", "12"))
        goal_label2_et4.grid(row=6, column=2, columnspan=1)
        goal_label2_et5 = Label(self, text="is", font=("Times", "14", "italic"))
        goal_label2_et5.grid(row=7, column=2, columnspan=1)
        goal_label2_et6 = Label(self, text="Preposition", font=("Times", "12"))
        goal_label2_et6.grid(row=6, column=3, columnspan=1)
        goal_label2_et7 = Label(self, text="in", font=("Times", "14", "italic"))
        goal_label2_et7.grid(row=7, column=3, columnspan=1)
        goal_label2_et8 = Label(self, text="Noun Phrase", font=("Times", "12"))
        goal_label2_et8.grid(row=6, column=4, columnspan=1)
        goal_label2_et9 = Label(self, text="the sentry-post", font=("Times", "14", "italic"))
        goal_label2_et9.grid(row=7, column=4, columnspan=1)

        goal_template2_label_list.extend([goal_label2_et1,goal_label2_et2,goal_label2_et3,goal_label2_et4,goal_label2_et5,
                                          goal_label2_et6,goal_label2_et7,goal_label2_et8,goal_label2_et9])
        self.chat_entry_labels.append(goal_template2_label_list)

        # Row 8: Goal Template 2 Text Input (Col 1-4)
        goal_template2_list = []    
        for i in range(1,5): 
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=8, column=i, columnspan=1, sticky=N+S+E+W)
            goal_template2_list.append(entry)
        self.chat_entries.append(goal_template2_list)
        
        # Row 9: Send Button (Col 5)
        # ToDo: enter check for all chat entries being populated with text
        self.submit_button = Button(self, text="Send", font=("Times", "18"))
        self.submit_button["command"] = lambda: self.send_combined_message()
        self.submit_button.grid(row=9, column=5, sticky=N+S+E+W)
        
        # Hide all template labels and entries at the beginning
        self.hide_template_entries()

    def on_exit(self):
        self.rosie_client.kill()
        if self.master:
            self.master.destroy()

    def send_message_to_rosie(self, message):
        if len(message) > 0:
            #Todo- Comment this for now but this needs to be adjusted based on whether you do different templates v/s one entry
            # self.chat_entry.add_to_history(message)
            for sublist in self.chat_entries:
                for i in range(len(sublist)):
                    sublist[i].clear()

            self.rosie_client.send_message(message)


def main():
    if len(sys.argv) == 1:
        print("ERROR: RosieGUI takes 1 argument - a rosie config file")
        return
    rosie_config = sys.argv[1]

    root = Tk()
    rosie_client = RosieClient(config_filename=rosie_config)
    rosie_gui = RosieGUI(rosie_client, master=root)
    rosie_gui.run()

if __name__ == "__main__":
    main()
