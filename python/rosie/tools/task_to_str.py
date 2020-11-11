""" Utility to print a task-operator in a standard formatting"""
from string import digits
from . import object_to_str

def task_to_str(task_id):
    """ Given an sml Identifier root of a task-operator, 
        returns a string representation of the task 
        e.g. 'move1(gray fork, on1(table))' """
    task_handle = task_id.GetChildString("task-handle")

    # Get the root identifier of each argument
    arg_list = ( (arg_name, task_id.GetChildId(arg_name)) for arg_name in ("arg1", "arg2", "arg3", "start-clause", "end-clause", "when-clause") )
    # Convert each non-null argument to a string
    parsed_args = ( task_arg_to_str(arg[0], arg[1]) for arg in arg_list if arg[1] is not None)

    return task_handle + "(" + ", ".join(parsed_args) + ")"

def task_arg_to_str(arg_name, arg_id):
    """ Given an sml Identifier root of a task argument, 
        returns a string representation of the argument """
    arg_type = arg_id.GetChildString("arg-type")
    if arg_type == "object":
        return object_to_str(arg_id.GetChildId("id"))
    elif arg_type == "partial-predicate":
        handle_str = arg_id.GetChildString("handle")
        obj2_str = object_to_str(arg_id.GetChildId("2"))
        return "%s(%s)" % ( handle_str, obj2_str )
    elif arg_type == "waypoint":
        wp_id = arg_id.GetChildId("id")
        return wp_id.GetChildString("handle")
    elif arg_type == "concept":
        return arg_id.GetChildString("handle")
    elif arg_type == "measure":
        return arg_id.GetChildString("number") + " " + arg_id.GetChildString("unit")
    elif arg_type == "coordinate":
        coord_id = arg_id.GetChildId("coord")
        return "{%.2f, %.2f}" % ( coord_id.GetChildFloat("x"), coord_id.GetChildFloat("y") )
    elif arg_type == "temporal-clause":
        return arg_name.split('-')[0] + pred_set_to_str(arg_id)
    return "?"

def pred_set_to_str(arg_id):
    """ Given an sml Identifier root of a predicate set, 
        returns a string representation of the predicate set
        e.g. '{ on1(red block, table), right1(red block, blue block) }' """
    num_preds = arg_id.GetChildInt("pred-count")
    parsed_preds = []
    for i in range(num_preds):
        pred = arg_id.GetChildId(str(i+1))
        parsed_preds.append(predicate_to_str(pred))
    return "{ " + ", ".join(parsed_preds) + " }"

def predicate_to_str(pred_id):
    """ Given an sml Identifier root of a predicate, 
        returns a string representation of the predicate
        e.g. 'on1(red block, table)' """
    pred_type = pred_id.GetChildString("type")
    pred_handle = pred_id.GetChildString("handle")
    if pred_type == "unary":
        obj1_str = object_to_str(pred_id.GetChildId("1"))
        return "%s(%s)" % (pred_handle, obj1_str)
    elif pred_type == "relation":
        obj1_str = object_to_str(pred_id.GetChildId("1"))
        obj2_str = object_to_str(pred_id.GetChildId("2"))
        return "%s(%s, %s)" % (pred_handle, obj1_str, obj2_str)
    elif pred_type == "clocktime":
        hour = pred_id.GetChildInt("hour")
        minute = pred_id.GetChildInt("minute")
        return "%d:%d" % (hour, minute)
    elif pred_type == "duration":
        number = pred_id.GetChildInt("number")
        unit = pred_id.GetChildString("unit")
        return "%d %s" % (number, unit)
    else:
        return "?"

