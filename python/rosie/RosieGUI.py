from tkinter import *
import tkinter.font

import sys

from . import RosieAgent
from rosie.events import AgentMessageSent, ScriptMessageSent, InstructorMessageSent

class RosieGUI(Frame):
    class MessagesList(Listbox):
        """ Displays a list of messages that are the interaction history """
        def __init__(self, parent):
            Listbox.__init__(self, parent, font=("Times", "12"), takefocus=0)
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
            Entry.__init__(self, parent, font=("Times", "16"))

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


    """ Creates a single chat box interface to send messages to Rosie and receive messages back """
    def __init__(self, rosie_agent, master):
        Frame.__init__(self, master, width=800, height=600)
        self.agent = rosie_agent

        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.create_widgets()
        self.setup_soar_agent()

        master.geometry("800x600")
        master.protocol("WM_DELETE_WINDOW", self.on_exit)

    def setup_soar_agent(self):
        """ Put any code that connects with the agent here """
        self.agent.add_event_handler(AgentMessageSent, lambda e: self.messages_list.add(e.message))
        self.agent.add_event_handler(InstructorMessageSent, lambda e: self.messages_list.add(e.message))
        self.agent.add_event_handler(ScriptMessageSent, lambda e: self.script_list.select_line(e.index))

    def run(self):
        self.agent.connect()
        self.master.mainloop()

    def create_widgets(self):
        self.grid(row=0, column=0, sticky=N+S+E+W)

        # 3 rows x 4 cols grid
        self.columnconfigure(0, weight=1, minsize=200)
        self.columnconfigure(1, weight=1, minsize=200)
        self.columnconfigure(2, weight=1, minsize=200)
        self.columnconfigure(3, weight=1, minsize=200)
        self.rowconfigure(0, weight=1, minsize=25)
        self.rowconfigure(1, weight=20, minsize=400)
        self.rowconfigure(2, weight=2, minsize=50)

        # Row 0: Top row of buttons for running the agent
        self.run_button = Button(self, text="Run", font=("Times", "18"))
        self.run_button["command"] = lambda: self.agent.start()
        self.run_button.grid(row=0, column=0, sticky=N+S+E+W)

        self.stop_button = Button(self, text="Stop", font=("Times", "18"))
        self.stop_button["command"] = lambda: self.agent.stop()
        self.stop_button.grid(row=0, column=1, sticky=N+S+E+W)

        # Row 1: Message History (Col 0-1) and Script Messages (Col 2-3)

        self.messages_list = RosieGUI.MessagesList(self)
        self.messages_list.grid(row=1, column=0, columnspan=2, sticky=N+S+E+W)

        self.script_list = RosieGUI.ScriptList(self, self.agent.messages)
        self.script_list.click_handler = lambda msg: self.send_message_to_rosie(msg)
        self.script_list.grid(row=1, column=2, columnspan=2, sticky=N+S+E+W)

        # Row 2: Message Entry (Col 0-2) and Send Button (Col 3)

        self.chat_entry = RosieGUI.ChatEntry(self)
        self.chat_entry.grid(row=2, column=0, columnspan=3, sticky=N+S+E+W)

        self.submit_button = Button(self, text="Send", font=("Times", "24"))
        self.submit_button["command"] = lambda: self.chat_entry.send_message()
        self.submit_button.grid(row=2, column=3, columnspan=1, sticky=N+S+E+W)

    def on_exit(self):
        self.agent.kill()
        if self.master:
            self.master.destroy()

    def send_message_to_rosie(self, message):
        if len(message) > 0:
            self.chat_entry.add_to_history(message)
            self.chat_entry.clear()
            self.agent.send_message(message)


def main():
    if len(sys.argv) == 1:
        print("ERROR: RosieGUI takes 1 argument - a rosie config file")
        return
    rosie_config = sys.argv[1]

    root = Tk()
    rosie_agent = RosieAgent(rosie_config)
    rosie_gui = RosieGUI(rosie_agent, master=root)
    rosie_gui.run()

if __name__ == "__main__":
    main()
