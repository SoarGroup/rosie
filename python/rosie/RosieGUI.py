import os
from pysoarlib import AgentConnector
import sys
from tkinter import *
from tkinter import messagebox
from ttkwidgets.autocomplete import AutocompleteEntryListbox

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


    """ Creates a single chat box interface to send messages to Rosie and receive messages back """
    def __init__(self, rosie_client, master):
        Frame.__init__(self, master, width=1000, height=600)
        self.rosie_client = rosie_client

        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.combined_message = ""
        self.listbox_entries = []
        self.listbox_entry_labels = []
        
        # Initialize lists of the different parts of speech for list dropdowns
        self.verb_list = []
        self.prep_list = []
        self.noun_list = []
        self.object_states_list = []

        self.create_widgets()
        self.setup_rosie_client()

        master.geometry("1366x768")
        master.protocol("WM_DELETE_WINDOW", self.on_exit)

    def setup_rosie_client(self):
        """ Put any code that connects with the rosie_client here """
        self.rosie_client.add_event_handler(AgentMessageSent, lambda e: self.messages_list.add(e.message))
        self.rosie_client.add_event_handler(InstructorMessageSent, lambda e: self.messages_list.add(e.message))
        self.rosie_client.add_event_handler(ScriptMessageSent, lambda e: self.script_list.select_line(e.index))
        self.rosie_client.add_connector("gui", GUIConnector(self.rosie_client, self))

    def run(self):
        self.rosie_client.connect()
        self.master.mainloop()

    """Import parts of speech lists from documents to python lists"""
    def create_parts_of_speech_lists(self):
        pos_folder_path = "/home/preetir/rosie-project/rosie/python/rosie/parts-of-speech-lists"

        # Create list of verbs
        self.verb_list = open(os.path.join(pos_folder_path, "verbs.txt")).read().splitlines()
        self.verb_list = sorted(self.verb_list)
        
        # Create list of prepositions
        self.prep_list = open(os.path.join(pos_folder_path, "prepositions.txt")).read().splitlines()
        self.prep_list = sorted(self.prep_list)
        
        # Create list of nouns
        self.noun_list = open(os.path.join(pos_folder_path, "nouns.txt")).read().splitlines()
        self.noun_list = sorted(self.noun_list)
        
        # Create list of being verbs
        self.being_verb_list = ["are","are not","contains","is","is not"]
        
        # Create list of object states
        self.object_states_list = open(os.path.join(pos_folder_path, "object-states.txt")).read().splitlines()
        self.object_states_list = sorted(self.object_states_list)

    def send_combined_message(self, message_type):
        self.combined_message = ""
        input_values = []

        if message_type == "done":
            self.combined_message += "You are done"
        else:
            validation_response = self.toggle_enable_send_button()
            if not validation_response[1]:
                messagebox.showinfo("Input Error", "Some inputs are empty or invalid.")
                for sublist in self.listbox_entries:
                    for i in range(len(sublist)):
                        sublist[i].clear()
                return
            else:
                input_values = validation_response[0]
                if self.last_selected_option == 0:
                    self.combined_message += input_values[0] + " the " + input_values[1]
                elif self.last_selected_option == 1:
                    self.combined_message += input_values[0] + " the "
                    for i in range(1,3):
                        self.combined_message += input_values[i] + " "
                    self.combined_message += "the " + input_values[3]
                elif self.last_selected_option == 2:
                    self.combined_message = "The goal is that the "
                    for i in range(0,3):
                        self.combined_message += input_values[i] + " "
                else:
                    self.combined_message = "The goal is that the "
                    for i in range(0,3):
                        self.combined_message += input_values[i] + " "
                    self.combined_message += "the " + input_values[3]

        self.combined_message = self.combined_message.strip() + "."
        self.send_message_to_rosie(self.combined_message)
        for sublist in self.listbox_entries:
            for i in range(len(sublist)):
                sublist[i].clear()

    """Populate listbox entries with predefined lists of parts of speech"""
    def populate_template_listboxes(self):
        # Populate verb listboxes
        self.listbox_entries[0][0].configure(completevalues=self.verb_list)
        self.listbox_entries[1][0].configure(completevalues=self.verb_list)

        # Populate being verb listboxes
        self.listbox_entries[2][1].configure(completevalues=self.being_verb_list)
        self.listbox_entries[3][1].configure(completevalues=self.being_verb_list)

        # Populate preposition listboxes
        self.listbox_entries[1][2].configure(completevalues=self.prep_list)
        self.listbox_entries[3][2].configure(completevalues=self.prep_list)   

        # Populate noun listboxes
        index_noun_list = [(0,1),(1,1),(1,3),(2,0),(3,0),(3,3)]
        for tuple in index_noun_list:
            self.listbox_entries[tuple[0]][tuple[1]].configure(completevalues=self.noun_list)

        # Populate object states listboxes
        self.listbox_entries[2][2].configure(completevalues=self.object_states_list)
    
    """ Function to hide all template labels and entries"""
    def hide_template_entries(self):
        for row in self.listbox_entries:
            for listbox_entry in row:
                listbox_entry.grid_remove()
        for row in self.listbox_entry_labels:
            for label in row:
                label.grid_remove()

    """ Function to show the template labels and entries linked to the selected option in the options menu"""
    def show_selected_template_entries(self, selected_index):
        for listbox_entry in self.listbox_entries[selected_index]:
            listbox_entry.grid()
        for label in self.listbox_entry_labels[selected_index]:
            label.grid()
    
    """Function to enable selected input template and disable the last selected template"""
    def enable_template_input(self, selected_index):
        if self.last_selected_option != -1:
            for listbox_t in self.listbox_entries[self.last_selected_option]:
                listbox_t.clear()
                listbox_t.grid_remove()
            for label in self.listbox_entry_labels[self.last_selected_option]:
                label.grid_remove()
        self.last_selected_option = selected_index
        self.show_selected_template_entries(selected_index)

    """Function to toggle enabling or disabling send button based on whether all entries are filled and valid"""
    def toggle_enable_send_button(self):
        num_of_template_inputs = len(self.listbox_entries[self.last_selected_option])
        # Copying text inputs from the template into list
        input_list = [self.listbox_entries[self.last_selected_option][i].get().strip() for i in range(num_of_template_inputs)]
        if '' in input_list:
            # if any of the inputs contains an empty value, return False
            return ([], False)
        else:
            # if any of the inputs contains an invalid value, return False
            for i in range(num_of_template_inputs):
                if input_list[i].lower() in list(map(lambda x: x.lower(),self.listbox_entries[self.last_selected_option][i].cget('completevalues'))):
                    continue
                else:
                    return ([], False)
        return (input_list, True)

    """Function to toggle enabling or disabling goal button based on whether Rosie requests goal"""
    def toggle_enable_goal_button(self, state):
        if state == "NORMAL":
            self.enable_gt1_button["state"] = NORMAL
            self.enable_gt2_button["state"] = NORMAL
        else:
            self.enable_gt1_button["state"] = DISABLED
            self.enable_gt2_button["state"] = DISABLED
            # Enable action templates when the goal buttons are disabled
            if self.last_selected_option in [2,3]:
                self.enable_template_input(0)
    
    def create_widgets(self):
        # ToDo: A way to clean up this code may be to get all the grid statements together and function statements(lambda etc) in another batch
        # https://tkdocs.com/tutorial/grid.html
        # https://www.askpython.com/python-modules/tkinter/python-tkinter-grid-example
        self.create_parts_of_speech_lists()
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
        self.rowconfigure(2, weight=2, minsize=50)
        self.rowconfigure(3, weight=2, minsize=50)

        # Action Templates 1 and 2
        self.rowconfigure(4, weight=2, minsize=30)
        self.rowconfigure(5, weight=2, minsize=30)

        # Goal Templates 1 and 2
        self.rowconfigure(6, weight=2, minsize=30)
        self.rowconfigure(7, weight=2, minsize=30)
        self.rowconfigure(8, weight=2, minsize=30)

        self.rowconfigure(9, weight=1, minsize=30)
        self.rowconfigure(10, weight=1, minsize=30)


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

        
        # Row 2: Buttons to enable/disable template inputs (Col 1-4)
        self.last_selected_option = -1

        self.enable_at1_button = Button(self, text="Ask robot to do something:\n Action + Object\n\"Turn on the alarm\"", font=("Times", "15"), padx=10)
        self.enable_at1_button["command"] = lambda: self.enable_template_input(0)
        self.enable_at1_button.grid(row=2, column=0, rowspan=2, sticky=N+S+E+W)

        self.enable_at2_button = Button(self, text="Ask robot to do something:\n Action + Object1 + Prep + Object2\n\"Fetch the coffee from the kitchen\"", font=("Times", "15"), padx=10)
        self.enable_at2_button["command"] = lambda: self.enable_template_input(1)
        self.enable_at2_button.grid(row=2, column=1, rowspan=2, columnspan=2, sticky=N+S+E+W)
 
        self.enable_gt1_button = Button(self, state=DISABLED, text="Provide desired world state\nfor robot to achieve:\nObject + Linking-verb + Object State\n\"The alarm is on\"", font=("Times", "15"), padx=10)
        self.enable_gt1_button["command"] = lambda: self.enable_template_input(2)
        self.enable_gt1_button.grid(row=2, column=3, rowspan=2, sticky=N+S+E+W)
 
        self.enable_gt2_button = Button(self, state=DISABLED, text="Provide desired world state\nfor robot to achieve:\n Object1 + Linking-verb + Prep + Object2\n\"The extinguisher is in the office\"", font=("Times", "15"), padx=10)
        self.enable_gt2_button["command"] = lambda: self.enable_template_input(3)
        self.enable_gt2_button.grid(row=2, column=4, rowspan=2, columnspan=2, sticky=N+S+E+W)
        

        # Row 3-8: Action and Goal Template Labels and Text Entries 

        # Row 3-4: Action Template 1 Labels (Col 0-1)
        action_template1_label_list = []
        label1_et1 = Label(self, text="Action", font=("Times", "14"))
        label1_et1.grid(row=5, column=0, columnspan=1)
        label1_et2 = Label(self, text="Turn on", font=("Times", "15", "italic"))
        label1_et2.grid(row=6, column=0, columnspan=1)
        label1_et3 = Label(self, text="Object", font=("Times", "14"))
        label1_et3.grid(row=5, column=2, columnspan=1)
        label1_et4 = Label(self, text="alarm", font=("Times", "15", "italic"))
        label1_et4.grid(row=6, column=2, columnspan=1)
        
        # Default "the" labels
        label1_et5 = Label(self, text="the\n\n\n\n\n\n", font=("Arial", "14"))
        label1_et5.grid(row=7, column=1, columnspan=1)
        action_template1_label_list.extend([label1_et1,label1_et2,label1_et3,label1_et4,label1_et5])
        self.listbox_entry_labels.append(action_template1_label_list)

        # Row 5: Action Template 1 Text Input (Col 0-1)
        action_template1_list = []
        for i in range(0,3):
            if i==1:
                continue
            else:
                listbox_entry = AutocompleteEntryListbox(self, font=("Arial", "12"))
                listbox_entry.grid(row=7, column=i, columnspan=1, sticky=N+S+E+W)

                action_template1_list.append(listbox_entry)
        self.listbox_entries.append(action_template1_list)

        
        # Row 3-4: Action Template 2 Labels (Col 0-3)
        action_template2_label_list = []
        label2_et1 = Label(self, text="Action", font=("Times", "14"))
        label2_et1.grid(row=5, column=0, columnspan=1)
        label2_et2 = Label(self, text="Fetch", font=("Times", "15", "italic"))
        label2_et2.grid(row=6, column=0, columnspan=1)
        label2_et3 = Label(self, text="Object1", font=("Times", "14"))
        label2_et3.grid(row=5, column=2, columnspan=1)
        label2_et4 = Label(self, text="coffee", font=("Times", "15", "italic"))
        label2_et4.grid(row=6, column=2, columnspan=1)
        label2_et5 = Label(self, text="Preposition", font=("Times", "14"))
        label2_et5.grid(row=5, column=3, columnspan=1)
        label2_et6 = Label(self, text="from", font=("Times", "15", "italic"))
        label2_et6.grid(row=6, column=3, columnspan=1)
        label2_et7 = Label(self, text="Object2", font=("Times", "14"))
        label2_et7.grid(row=5, column=5, columnspan=1)
        label2_et8 = Label(self, text="kitchen", font=("Times", "15", "italic"))
        label2_et8.grid(row=6, column=5, columnspan=1)
        
         # Default "the" labels
        label2_et9 = Label(self, text="the\n\n\n\n\n\n", font=("Arial", "14"))
        label2_et9.grid(row=7, column=1, columnspan=1)
        label2_et10 = Label(self, text="the\n\n\n\n\n\n", font=("Arial", "14"))
        label2_et10.grid(row=7, column=4, columnspan=1)
        for i in range(1,11):
            action_template2_label_list.append(locals()["label2_et" + str(i)])
        self.listbox_entry_labels.append(action_template2_label_list)
        
        # Row 5: Action Template 2 Text Input (Col 0-3)
        action_template2_list = []
        for i in range(0,6):
            if i in (1,4):
                continue
            else:
                listbox_entry = AutocompleteEntryListbox(self, font=("Arial", "12"))
                listbox_entry.grid(row=7, column=i, columnspan=1, sticky=N+S+E+W)
                action_template2_list.append(listbox_entry)        
        self.listbox_entries.append(action_template2_list)


        # Row 6-8: Goal Template 1
        goal_template1_label_list = []
        # Goal Template 1 - Chat Entry Default Label
        goal_label1_et1 = Label(self, text="The goal is that the\n\n\n\n\n", font=("Times", "15"))
        goal_label1_et1.grid(row=7, column=0, columnspan=1)

        # Goal Template 1 Labels (Col 1-3)
        goal_label1_et2 = Label(self, text="Object", font=("Times", "14"))
        goal_label1_et2.grid(row=5, column=1, columnspan=1)
        goal_label1_et3 = Label(self, text="alarm", font=("Times", "15", "italic"))
        goal_label1_et3.grid(row=6, column=1, columnspan=1)
        goal_label1_et4 = Label(self, text="Linking-verb", font=("Times", "14"))
        goal_label1_et4.grid(row=5, column=2, columnspan=1)
        goal_label1_et5 = Label(self, text="is", font=("Times", "15", "italic"))
        goal_label1_et5.grid(row=6, column=2, columnspan=1)
        goal_label1_et6 = Label(self, text="Object State", font=("Times", "14"))
        goal_label1_et6.grid(row=5, column=3, columnspan=1)
        goal_label1_et7 = Label(self, text="on", font=("Times", "15", "italic"))
        goal_label1_et7.grid(row=6, column=3, columnspan=1)
        
        for i in range(1,8):
            goal_template1_label_list.append(locals()["goal_label1_et" + str(i)])
        self.listbox_entry_labels.append(goal_template1_label_list)

        # Row 8: Goal Template 1 Text Input (Col 1-3)
        goal_template1_list = []
        for i in range(1,4):
            listbox_entry = AutocompleteEntryListbox(self, font=("Arial", "12"))
            listbox_entry.grid(row=7, column=i, columnspan=1, sticky=N+S+E+W)
            goal_template1_list.append(listbox_entry)
        self.listbox_entries.append(goal_template1_list)  


        # Row 6-8: Goal Template 2
        goal_template2_label_list = []
        # Goal Template 2 - Chat Entry Default Labels
        goal_label2_et1 = Label(self, text="The goal is that the\n\n\n\n\n", font=("Times", "15"))
        goal_label2_et1.grid(row=7, column=0, columnspan=1)        
        goal_label2_et2 = Label(self, text="the\n\n\n\n\n\n", font=("Arial", "14"))
        goal_label2_et2.grid(row=7, column=4, columnspan=1)

        # Goal Template 2 Labels (Col 1-3)
        goal_label2_et3 = Label(self, text="Object1", font=("Times", "14"))
        goal_label2_et3.grid(row=5, column=1, columnspan=1)
        goal_label2_et4 = Label(self, text="extinguisher", font=("Times", "15", "italic"))
        goal_label2_et4.grid(row=6, column=1, columnspan=1)
        goal_label2_et5 = Label(self, text="Linking-verb", font=("Times", "14"))
        goal_label2_et5.grid(row=5, column=2, columnspan=1)
        goal_label2_et6 = Label(self, text="is", font=("Times", "15", "italic"))
        goal_label2_et6.grid(row=6, column=2, columnspan=1)
        goal_label2_et7 = Label(self, text="Preposition", font=("Times", "14"))
        goal_label2_et7.grid(row=5, column=3, columnspan=1)
        goal_label2_et8 = Label(self, text="in", font=("Times", "15", "italic"))
        goal_label2_et8.grid(row=6, column=3, columnspan=1)
        goal_label2_et9 = Label(self, text="Object2", font=("Times", "14"))
        goal_label2_et9.grid(row=5, column=5, columnspan=1)
        goal_label2_et10 = Label(self, text="office", font=("Times", "15", "italic"))
        goal_label2_et10.grid(row=6, column=5, columnspan=1)
        
        for i in range(1,11):
            goal_template2_label_list.append(locals()["goal_label2_et" + str(i)])
        self.listbox_entry_labels.append(goal_template2_label_list)

        # Row 8: Goal Template 2 Text Input (Col 1-4)
        goal_template2_list = []    
        for i in range(1,6):
            if i==4:
                continue
            else:
                listbox_entry = AutocompleteEntryListbox(self, font=("Arial", "12"))
                listbox_entry.grid(row=7, column=i, columnspan=1, sticky=N+S+E+W)
                goal_template2_list.append(listbox_entry)
        self.listbox_entries.append(goal_template2_list)

        
        # Row 9: "You are done" message (Col 0) and Send Button (Col 5)
        self.task_done_button = Button(self, text="You are done", font=("Times", "18"))
        self.task_done_button["command"] = lambda: self.send_combined_message("done")
        self.task_done_button.grid(row=9, column=0, rowspan=2, sticky=N+S+E+W)
        
        self.submit_button = Button(self, text="Send", font=("Times", "18"))
        self.submit_button["command"] = lambda: self.send_combined_message("")
        self.submit_button.grid(row=9, column=5, rowspan=2, sticky=N+S+E+W)
        
        # Populate all the listboxes with individual POS lists
        self.populate_template_listboxes()       
 
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
            for sublist in self.listbox_entries:
                for i in range(len(sublist)):
                    sublist[i].clear()

            self.rosie_client.send_message(message)

class GUIConnector(AgentConnector):
    """ Will connect GUI to soar agent input and output
        For input - N/A
        For output - will take a message type and modify GUI controls based on message type """
    def __init__(self, client, rosie_gui):
        AgentConnector.__init__(self, client)
        self.add_output_command("send-message")
        self.rosie_gui = rosie_gui

    def on_output_event(self, command_name, root_id):
        if command_name == "send-message":
            message_type = root_id.FindByAttribute("type", 0).GetValueAsString()
            if not message_type:
                root_id.CreateStringWME("status", "error")
                root_id.CreateStringWME("error-info", "send-message has no type")
                self.client.print_handler("GUIConnector: Error - send-message has no type")
                return

            if message_type == "get-next-goal":
                self.rosie_gui.toggle_enable_goal_button("NORMAL")
            else:
                self.rosie_gui.toggle_enable_goal_button("DISABLED")


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
