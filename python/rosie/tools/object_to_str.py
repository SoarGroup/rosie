""" Utility method to take a rosie object and return a string description """

from string import digits

def strip_digits(s):
    return s.translate(str.maketrans('', '', digits))

def get_value(wmes, id, attr):
    return next((wme[2] for wme in wmes.get(id, []) if wme[1] == attr), None)

def object_to_str(obj_id):
    """ Given an sml Identifier root of a world object, 
        returns a string representation of the object 
        e.g. 'red block' """
    words = []
    preds_id = obj_id.GetChildId("predicates")
    # If the object has a sentence, use that
    sent = preds_id.GetChildString("sentence")
    if sent is not None:
        return sent

    words.append(preds_id.GetChildString("size"))
    words.append(preds_id.GetChildString("color"))
    mods = preds_id.GetAllChildValues("modifier1")
    if len(mods) > 0:
        words.append(sorted(mods)[0])
    words.append(preds_id.GetChildString("shape"))

    name = preds_id.GetChildString("name")
    if name is None: name = obj_id.GetChildString("root-category")
    if name is None: name = preds_id.GetChildString("category")
    words.append(name)

    obj_desc = " ".join(word for word in words if word is not None)
    return strip_digits(obj_desc)

