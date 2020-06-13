""" Used in the Rosie project to parse messages from the Rosie Agent

    Handles send-message commands on the output link
"""
import sys

from string import digits
from pysoarlib import SoarUtils

strip_digits = lambda s: s.translate(str.maketrans('', '', digits))

task_handle = lambda fields: strip_digits(fields['task-handle'])
world_obj = lambda fields: RosieMessageParser.parse_obj(fields['object']['__id__'])

class RosieMessageParser:
    def parse_message(root_id, message_type):
        msg_graph = SoarUtils.extract_wm_graph(root_id)
        fields = msg_graph.get('fields', None)
        message_parser = RosieMessageParser.message_parser_map.get(message_type)
        if message_parser is not None and callable(message_parser):
            return message_parser(fields)
        return message_type

    def parse_obj(obj_id):
        words = []
        preds_id = obj_id.GetChildId("predicates")
        # If the object has a sentence, use that
        sent = preds_id.GetChildString("sentence")
        if sent is not None:
            return sent

        words.append(preds_id.GetChildString("size"))
        words.append(preds_id.GetChildString("color"))
        words.append(preds_id.GetChildString("modifier1"))
        words.append(preds_id.GetChildString("shape"))

        name = preds_id.GetChildString("name")
        words.append(name if name is not None else obj_id.GetChildString("root-category"))
        obj_desc = " ".join(word for word in words if word is not None)
        return strip_digits(obj_desc)

    message_parser_map = {
        "ok": lambda fields: "Ok",
        "unable-to-satisfy": lambda fields: "I couldn't do that",
        "unable-to-interpret-message": lambda fields: "I don't understand.",
        "missing-object": lambda fields: "I lost the object I was using. Can you help me find it?",
        "index-object-failure": lambda fields: "I couldn't find the referenced object",
        "no-proposed-action": lambda fields: "I couldn't do that",
        "missing-argument": lambda fields: "I need more information to do that action",
        "learn-location-failure": lambda fields: "I don't know where I am.",
        "get-next-goal": lambda fields: "What is the next goal or subtask of " + task_handle(fields) + "?",
        "no-action-context-for-goal": lambda fields: "I don't know what action that goal is for",
        "get-next-task": lambda fields: "I'm ready for a new task",
        "get-next-subaction": lambda fields: "What do I do next for " + task_handle(fields) + "?",
        "confirm-pick-up": lambda fields: "I have picked up the object.",
        "confirm-put-down": lambda fields: "I have put down the object.",
        "stop-leading": lambda fields: "You can stop following me",
        "retrospective-learning-failure": lambda fields: "I was unable to learn the task policy",
        "report-successful-training": lambda fields: "Ok",
        "task-execution-failure": lambda fields: "The " + task_handle(fields) + " task failed.",
        
        #added for games and puzzles
        "your-turn": lambda fields: "Your turn.",
        "i-win": lambda fields: "I win!",
        "i-lose": lambda fields: "I lose.",
        "easy": lambda fields: "That was easy!",
        "describe-game": lambda fields: "Please setup the game.",
        "describe-puzzle": lambda fields: "Please setup the puzzle.",
        "setup-goal": lambda fields: "Please setup the goal state.",
        "tell-me-go": lambda fields: "Ok: tell me when to go.",
        "setup-failure": lambda fields: "Please setup the failure condition.",
        "define-actions": lambda fields: "Can you describe the legal actions?",
        "describe-action": lambda fields: "What are the conditions of the action.",
        "describe-goal": lambda fields: "Please describe or demonstrate the goal.",
        "describe-failure": lambda fields: "Please describe the failure condition.",
        "learned-goal": lambda fields: "I have learned the goal.",
        "learned-action": lambda fields: "I have learned the action.",
        "learned-failure": lambda fields: "I have learned the failure condition.",
        "learned-heuristic": lambda fields: "I have learned the heuristic.",
        "already-learned-goal": lambda fields: "I know that goal and can recognize it.",
        "already-learned-action": lambda fields: "I know that action and can recognize it.",
        "already-learned-failure": lambda fields: "I know that failure condition and can recognize it.",
        "gotit": lambda fields: "I've found a solution.",

        "single-word-message": lambda fields: fields['word'],
        "say-sentence": lambda fields: fields['sentence'],
        "agent-object-description": lambda fields: world_obj(fields),
        "cant-find-object": lambda fields: "I can't find " + world_obj(fields) + \
            ", can you help?"
    }

