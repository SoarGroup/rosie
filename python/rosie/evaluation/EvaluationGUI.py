from tkinter import *
import tkinter.font

import sys

from rosie.evaluation import EvaluationAgent

class EvaluationGUI(Frame):
    def create_widgets(self):
        self.grid(row=0, column=0, sticky=N+S+E+W)

        self.columnconfigure(0, weight=1, minsize=200)
        self.columnconfigure(1, weight=1, minsize=200)
        self.columnconfigure(2, weight=1, minsize=200)
        self.columnconfigure(3, weight=1, minsize=200)
        self.rowconfigure(0, weight=1, minsize=25)
        self.rowconfigure(1, weight=20, minsize=400)
        self.rowconfigure(2, weight=2, minsize=50)

        # Row 0: Top row of buttons

        self.run_button = Button(self, text="Run", font=("Times", "18"))
        self.run_button["command"] = self.on_run_click
        self.run_button.grid(row=0, column=0, sticky=N+S+E+W)

        self.stop_button = Button(self, text="Stop", font=("Times", "18"))
        self.stop_button["command"] = self.on_stop_click
        self.stop_button.grid(row=0, column=1, sticky=N+S+E+W)

        self.reset_button = Button(self, text="Reset", font=("Times", "18"))
        self.reset_button["command"] = self.on_reset_click
        self.reset_button.grid(row=0, column=2, sticky=N+S+E+W)


        # Row 1: Chat History (col 0-1), Script Messages (col 2-3)
        self.messages_list = Listbox(self, font=("Times", "12"))
        self.scrollbar = Scrollbar(self.messages_list)
        self.messages_list.config(yscrollcommand=self.scrollbar.set)
        self.scrollbar.config(command=self.messages_list.yview)
        self.messages_list.grid(row=1, column=0, columnspan=2, sticky=N+S+E+W)
        self.scrollbar.pack(side=RIGHT, fill=Y)

        self.script_list = Listbox(self, font=("Times", "12"))
        self.script_scrollbar = Scrollbar(self.script_list)
        self.script_list.config(yscrollcommand=self.script_scrollbar.set)
        self.script_scrollbar.config(command=self.script_list.yview)
        self.script_list.grid(row=1, column=2, columnspan=2, sticky=N+S+E+W)
        self.script_scrollbar.pack(side=RIGHT, fill=Y)

        # Row 2: Message Entry (Col 0-2) and Send Button (Col 3)
        self.chat_entry = Entry(self, font=("Times", "16"))
        self.chat_entry.bind('<Return>', lambda key: self.on_submit_click())
        self.chat_entry.bind('<Up>', lambda key: self.scroll_history(-1))
        self.chat_entry.bind('<Down>', lambda key: self.scroll_history(1))
        self.chat_entry.grid(row=2, column=0, columnspan=3, sticky=N+S+E+W)

        self.submit_button = Button(self, text="Send", font=("Times", "24"))
        self.submit_button["command"] = self.on_submit_click
        self.submit_button.grid(row=2, column=3, columnspan=1, sticky=N+S+E+W)

    def init_soar_agent(self, config_file):
        self.agent = EvaluationAgent(self, config_filename=config_file)
        self.agent.connect()

    def read_script(self):
        self.script_index = 0
        self.script = []
        if self.agent.messages_file != None:
            with open(self.agent.messages_file, 'r') as f:
                self.script = [ line.rstrip('\n') for line in f.readlines() if len(line.rstrip('\n')) > 0 and line[0] != '#']

        for message in self.script:
            self.script_list.insert(END, message)

        self.script_list.select_set(0) #This only sets focus on the first item.
        self.script_list.event_generate("<<ListboxSelect>>")

    def get_next_script_message(self):
        if self.script_index < len(self.script):
            self.script_list.select_set(self.script_index) 
            self.script_list.event_generate("<<ListboxSelect>>")
            self.script_index += 1
            self.script_list.see(self.script_index)
            return self.script[self.script_index-1]
        return None

    def append_message(self, message):
        self.messages_list.insert(END, message)
        self.messages_list.see(END)

    def send_message_to_agent(self, message):
        self.messages_list.insert(END, message)
        self.messages_list.see(END)
        self.chat_entry.delete(0, END)
        if len(self.message_history) == 0 or self.message_history[-1] != message:
            self.message_history.append(message)
        self.history_index = len(self.message_history)
        # If it starts with a !, it's a special command
        if message[0] == '!':
            self.agent.handle_script_command(message[1:])
        else:
            self.agent.connectors["language"].send_message(message)

    def on_submit_click(self):
        self.send_message_to_agent(self.chat_entry.get())
        
    def on_run_click(self):
        self.agent.start()

    def on_stop_click(self):
        self.agent.stop()

    def on_reset_click(self):
        self.agent.kill()
        self.message_history = []
        self.history_index = 0
        self.script_index = 0
        self.script_list.select_set(0) 
        self.chat_entry.delete(0, END)
        self.messages_list.delete(0, END)
        
        self.init_soar_agent(self.rosie_config_file)

    def scroll_history(self, delta):
        if self.history_index == 0 and delta == -1:
            return
        if self.history_index == len(self.message_history) and delta == 1:
            return

        self.history_index += delta
        self.chat_entry.delete(0, END)
        if self.history_index < len(self.message_history):
            self.chat_entry.insert(END, self.message_history[self.history_index])

    def on_exit(self):
        self.agent.kill()
        if self.master:
            self.master.destroy()

    def __init__(self, rosie_config, master=None):
        Frame.__init__(self, master, width=800, height=600)
        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.message_history = []
        self.history_index = 0

        self.create_widgets()
        self.rosie_config_file = rosie_config
        self.init_soar_agent(rosie_config)
        self.read_script()

