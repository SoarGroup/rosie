from tkinter import *
import tkinter.font

import sys

class RosieGUI(Frame):
    """ Creates a single chat box interface to send messages to Rosie and receive messages back """
    def __init__(self, agent_factory, master=None):
        """ agent_factory is a method taking no arguments that returns an instance of a RosieAgent """
        Frame.__init__(self, master, width=800, height=600)
        self.master = master
        master.columnconfigure(0, weight=1)
        master.rowconfigure(0, weight=1)

        self.message_history = []
        self.history_index = 0

        self.create_widgets()
        self.init_soar_agent(agent_factory)
        self.create_script_buttons()
        master.geometry("800x600")

    def create_widgets(self):
        self.grid(row=0, column=0, sticky=N+S+E+W)
        self.columnconfigure(0, weight=3, minsize=200)
        self.columnconfigure(1, weight=1, minsize=80)
        self.columnconfigure(2, weight=1, minsize=80)
        self.rowconfigure(0, weight=10, minsize=300)
        self.rowconfigure(1, weight=1, minsize=50)

        self.messages_list = Listbox(self, font=("Times", "12"))
        self.scrollbar = Scrollbar(self.messages_list)
        self.messages_list.config(yscrollcommand=self.scrollbar.set)
        self.scrollbar.config(command=self.messages_list.yview)
        self.messages_list.grid(row=0, column=0, sticky=N+S+E+W)
        self.scrollbar.pack(side=RIGHT, fill=Y)

        self.script_canvas = Canvas(self)
        self.script_scroll = Scrollbar(self.script_canvas, orient="vertical", command=self.script_canvas.yview)
        self.script_frame = Frame(self.script_canvas)
        self.script_frame.bind("<Configure>", 
                lambda e: self.script_canvas.configure(scrollregion=self.script_canvas.bbox("all"))
        )
        self.script_canvas.create_window((0, 0), window=self.script_frame, anchor="nw")
        self.script_canvas.configure(yscrollcommand=self.script_scroll.set)
        self.script_canvas.grid(row=0, column=1, columnspan=2, sticky=N+S+E+W)

        self.chat_entry = Entry(self, font=("Times", "16"))
        self.chat_entry.bind('<Return>', lambda key: self.on_submit_click())
        self.chat_entry.bind('<Up>', lambda key: self.scroll_history(-1))
        self.chat_entry.bind('<Down>', lambda key: self.scroll_history(1))
        self.chat_entry.grid(row=1, column=0, sticky=N+S+E+W)

        self.submit_button = Button(self, text="Send", font=("Times", "24"))
        self.submit_button["command"] = self.on_submit_click
        self.submit_button.grid(row=1, column=1, sticky=N+S+E+W)

        self.run_button = Button(self, text="Run", font=("Times", "24"))
        self.run_button["command"] = self.on_run_click
        self.run_button.grid(row=1, column=2, sticky=N+S+E+W)

    def init_soar_agent(self, agent_factory):
        self.agent = agent_factory()
        self.agent.connectors["language"].register_message_callback(self.receive_message)
        self.agent.connect()

    def create_script_buttons(self):
        self.script = []
        if self.agent.messages_file != None:
            with open(self.agent.messages_file, 'r') as f:
                self.script = [ line.rstrip('\n') for line in f.readlines() if len(line.strip()) > 0 and line[0] != '#']

        row = 0
        for message in self.script:
            button = Button(self.script_frame, text=message[:50], font=("Times", "10"))
            button["command"] = lambda message=message: self.send_message(message)
            button.pack(fill="x")
            row += 1

        self.script_scroll.pack(side=RIGHT, fill=Y)


    def send_message(self, message):
        self.messages_list.insert(END, message)
        self.chat_entry.delete(0, END)
        if len(self.message_history) == 0 or self.message_history[-1] != message:
            self.message_history.append(message)
        self.history_index = len(self.message_history)
        self.agent.connectors["language"].send_message(message)

    def receive_message(self, message):
        self.messages_list.insert(END, message)

    def on_submit_click(self):
        self.send_message(self.chat_entry.get())
        
    def on_run_click(self):
        self.agent.start()

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


