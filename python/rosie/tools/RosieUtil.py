from DebuggerUtil import parse_wm_printout

get_value = lambda wmes, id, attr: next((wme[2] for wme in wmes.get(id, []) if wme[1] == attr), None)

def pretty_print_world(text):
    return_str = ""

    # World ID
    world_id = text.split()[0].replace('(', '')
    wmes = parse_wm_printout(text)

    status_preds = [ ('is-confirmed1', 'confirmed1'),
                     ('is-visible1', 'visible1'),
                     ('is-reachable1', 'reachable1'),
                     ('is-grabbed1', 'grabbed1') ]
    ignore_preds = [ p[0] for p in status_preds ] + [ 'category' ]

    return_str = "{:<6}{:<18}{:<18}|conf | vis |reach|grab | other predicates\n".format('id', 'handle', 'category')
    return_str += "----------------------------------------------------------------------------------------------------------------\n"

    objs_id = get_value(wmes, world_id, 'objects')
    # List of object identifiers
    obj_ids = [ wme[2] for wme in wmes[objs_id] if wme[1] == 'object' ]
    for obj_id in obj_ids:
        obj_str = ''
        # Part 1: identifier
        obj_str += "{:<6}".format(obj_id)

        # Part 2: handle
        obj_handle = get_value(wmes, obj_id, 'handle')
        obj_str += "{:<18}".format(obj_handle)
        
        # Part 3: root category
        obj_cat = get_value(wmes, obj_id, 'root-category')
        obj_str += "{:<18}".format(obj_cat)

        # Part 4: status predicates
        preds_id = get_value(wmes, obj_id, 'predicates')
        for sp in status_preds:
            val = get_value(wmes, preds_id, sp[0])
            is_true = (val is not None and val == sp[1])
            obj_str += "|{:^5}".format('X' if is_true else ' ')

        # Part 5: other predicates
        preds = [ wme[2] for wme in wmes[preds_id] if wme[1] not in ignore_preds ]
        obj_str += '| ' + ' '.join(preds)
        return_str += obj_str + '\n'

    return_str += '\nRELATIONS:\n'

    rels_id = get_value(wmes, world_id, 'predicates')
    rel_ids = [ wme[2] for wme in wmes[rels_id] if wme[1] == 'predicate' ]
    for rel_id in rel_ids:
        rel_handle = get_value(wmes, rel_id, 'handle')
        return_str += rel_handle + '\n'
        for ins in ( wme[2] for wme in wmes[rel_id] if wme[1] == 'instance' ):
            obj1 = get_value(wmes, ins, '1')
            obj1 = get_value(wmes, obj1, 'root-category')
            obj2 = get_value(wmes, ins, '2')
            obj2 = get_value(wmes, obj2, 'root-category')
            return_str += "  {}({}, {})\n".format(rel_handle, obj1, obj2)

    return return_str

