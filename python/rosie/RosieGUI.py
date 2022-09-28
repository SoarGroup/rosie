from tkinter import *
import tkinter.font

import sys

from . import RosieClient
from rosie.events import AgentMessageSent, ScriptMessageSent, InstructorMessageSent

class RosieGUI(Frame):
    class MessagesList(Listbox):
        """ Displays a list of messages that are the interaction history """
        def __init__(self, parent):
            Listbox.__init__(self, parent, font=("Times", "12"), takefocus=0)
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
            Listbox.__init__(self, parent, font=("Times", "10"), selectmode=SINGLE)
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
            Entry.__init__(self, parent, font=("Times", "16"), state="disabled")
            self.parent = parent

            self.bind('<Return>', lambda key: self.send_message())
            self.bind('<Up>', lambda key: self.scroll_history(-1))
            self.bind('<Down>', lambda key: self.scroll_history(1))

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

    class SwitchTextBoxes:
        def __init__(self, parent):
            self.parent = parent
        
        def enable_template_input(self, temp_type):
            #default = "Incorrect Input"
            [sublist[i].config(state = "disabled") for sublist in self.parent.chat_entries for i in range(len(sublist))] 
            if temp_type == "AT1":
                [self.parent.chat_entries[0] [i-2].config(state = "normal") for i in range(2,4)]
            elif temp_type == "AT2":
                [self.parent.chat_entries[1] [i-2].config(state = "normal") for i in range(2,6)]
            elif temp_type == "GT1":
                [self.parent.chat_entries[2] [i-2].config(state = "normal") for i in range(2,5)]
            else:
                [self.parent.chat_entries[3] [i-2].config(state = "normal") for i in range(2,6)]

    """ Creates a single chat box interface to send messages to Rosie and receive messages back """
    def __init__(self, rosie_client, master):
        Frame.__init__(self, master, width=1000, height=600)
        self.rosie_client = rosie_client

        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.combined_message = ""
        self.chat_entries = []

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
        self.combined_message = ""
        for text in self.chat_entries:
            self.combined_message += text.get().strip() + " "
        self.combined_message = self.combined_message.strip() + "."
        self.send_message_to_rosie(self.combined_message)
        # need to clear all the chat_entries
        for text in self.chat_entries:
            text.clear()

    def enable_action_template1(self):
        for id in range(2,4):
            self.chat_entries[id].config(state = "disabled")

    def enable_action_template2(self):
        for id in range(2,4):
            self.chat_entries[id].config(state = "normal")

    def create_widgets(self):
        self.grid(row=0, column=0, sticky=N+S+E+W)

        # 8 rows x 6 cols grid
        
        # ToDo - Need to set the num_columns based on the template
        self.num_columns = 4
        
        self.columnconfigure(0, weight=1, minsize=150)
        self.columnconfigure(1, weight=1, minsize=100)
        self.columnconfigure(2, weight=1, minsize=100)
        self.columnconfigure(3, weight=1, minsize=100)
        self.columnconfigure(4, weight=1, minsize=100)
        self.columnconfigure(5, weight=1, minsize=100)
        self.columnconfigure(6, weight=1, minsize=100)
        self.columnconfigure(7, weight=1, minsize=100)
        self.rowconfigure(0, weight=1, minsize=25)
        self.rowconfigure(1, weight=20, minsize=400)
        self.rowconfigure(2, weight=2, minsize=50)
        self.rowconfigure(3, weight=2, minsize=50)
        self.rowconfigure(4, weight=2, minsize=50)
        self.rowconfigure(5, weight=2, minsize=50)


        # Row 0: Top row of buttons for running Rosie
        self.run_button = Button(self, text="Run", font=("Times", "18"))
        self.run_button["command"] = lambda: self.rosie_client.start()
        self.run_button.grid(row=0, column=0, columnspan=2, sticky=N+S+E+W)

        self.stop_button = Button(self, text="Stop", font=("Times", "18"))
        self.stop_button["command"] = lambda: self.rosie_client.stop()
        self.stop_button.grid(row=0, column=2, columnspan=2, sticky=N+S+E+W)


        # Row 1: Message History (Col 0-4) and Script Messages (Col 5-7)
        self.messages_list = RosieGUI.MessagesList(self)
        self.messages_list.grid(row=1, column=0, columnspan=5, sticky=N+S+E+W)

        print("AGENT MESSAGES")
        print(self.rosie_client.messages)
        self.script_list = RosieGUI.ScriptList(self, self.rosie_client.messages)
        self.script_list.click_handler = lambda msg: self.send_message_to_rosie(msg)
        self.script_list.grid(row=1, column=5, columnspan=3, sticky=N+S+E+W)


        # Col 0: Input examples for each template (Row 2-5)
        for i in range(2,6):
            label = Label(self, text="Test", font=("Times", "12"))
            label.grid(row=i, column=0, columnspan=1)

        self.switch_enable = RosieGUI.SwitchTextBoxes(self)
        
        # Col 1: Buttons to enable/disable template inputs (Row 2-5)
        self.enable_at1_button = Button(self, text="Action \n Template 1", font=("Times", "12"))
        self.enable_at1_button["command"] = lambda: self.switch_enable.enable_template_input("AT1")
        self.enable_at1_button.grid(row=2, column=1, sticky=N+S+E+W)

        self.enable_at2_button = Button(self, text="Action \n Template 2", font=("Times", "12"))
        self.enable_at2_button["command"] = lambda: self.switch_enable.enable_template_input("AT2")
        self.enable_at2_button.grid(row=3, column=1, sticky=N+S+E+W)

        self.enable_gt1_button = Button(self, text="Goal \n Template 1", font=("Times", "12"))
        self.enable_gt1_button["command"] = lambda: self.switch_enable.enable_template_input("GT1")
        self.enable_gt1_button.grid(row=4, column=1, sticky=N+S+E+W)

        self.enable_gt2_button = Button(self, text="Goal \n Template 2", font=("Times", "12"))
        self.enable_gt2_button["command"] = lambda: self.switch_enable.enable_template_input("GT2")
        self.enable_gt2_button.grid(row=5, column=1, sticky=N+S+E+W)

        
        # Todo - Consider adding text in labels based on the template chosen
        # For example, if participant chooses AT1, they will see "Verb + Noun Phrase"

   
        # Row 2: Action Template 1 Text Input (Col 2-3)
        action_template1_list = []
        for i in range(2,4):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=2, column=i, columnspan=1, sticky=N+S+E+W)
            action_template1_list.append(entry)
        self.chat_entries.append(action_template1_list)
        
        
        # Row 3: Action Template 2 Text Input (Col 2-5)
        action_template2_list = []
        for i in range(2,6):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=3, column=i, columnspan=1, sticky=N+S+E+W)
            action_template2_list.append(entry)        
        self.chat_entries.append(action_template2_list)
        
        
        # Row 4: Goal Template 1 Text Input (Col 2-5)
        goal_label_1 = Label(self, text="The goal is that", font=("Times", "12"))
        goal_label_1.grid(row=4, column=2, columnspan=1)

        goal_template1_list = []
        for i in range(3,6):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=4, column=i, columnspan=1, sticky=N+S+E+W)
            goal_template1_list.append(entry)
        self.chat_entries.append(goal_template1_list)  


        # Row 5: Goal Template 2 Text Input (Col 2-6)
        goal_label_2 = Label(self, text="The goal is that", font=("Times", "12"))
        goal_label_2.grid(row=5, column=2, columnspan=1)

        goal_template2_list = []    
        for i in range(3,7):
            entry = RosieGUI.ChatEntry(self)
            entry.grid(row=5, column=i, columnspan=1, sticky=N+S+E+W)
            goal_template2_list.append(entry)
        self.chat_entries.append(goal_template2_list)


        # Col 7: Send Button (Row 3-4)
        # ToDo: enter check for all chat entries being populated with text
        self.submit_button = Button(self, text="Send", font=("Times", "16"))
        self.submit_button["command"] = lambda: self.send_combined_message()
        self.submit_button.grid(row=3, column=7, rowspan=2, sticky=N+S+E+W)

    def on_exit(self):
        self.rosie_client.kill()
        if self.master:
            self.master.destroy()

    def send_message_to_rosie(self, message):
        if len(message) > 0:
            #Todo- Comment this for now but this needs to be adjusted based on whether you do different templates v/s one entry
            # self.chat_entry.add_to_history(message)
            for text in self.chat_entries:
                text.clear()

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
