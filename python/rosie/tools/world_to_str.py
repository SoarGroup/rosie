def world_to_str(world_id):
    """ Given a rosie world rooted at the given Identifier, 
        will return a nicely formatted string summary """
    return_str = ""

    # World ID
    status_preds = [ ('is-confirmed1', 'confirmed1'),
                     ('is-visible1', 'visible1'),
                     ('is-reachable1', 'reachable1'),
                     ('is-grabbed1', 'grabbed1') ]
    ignore_preds = [ p[0] for p in status_preds ] + [ 'category' ]

    return_str = "{:<6}{:<18}{:<18}|conf | vis |reach|grab | other predicates\n".format('id', 'handle', 'category')
    return_str += "----------------------------------------------------------------------------------------------------------------\n"

    # List of object identifiers
    objs_id = world_id.GetChildId('objects')
    obj_ids = objs_id.GetAllChildIds('object')
    for obj_id in obj_ids:
        obj_str = ''
        # Part 1: identifier
        obj_str += "{:<6}".format(obj_id.GetIdentifierSymbol())

        # Part 2: handle
        obj_handle = obj_id.GetChildString('handle')
        obj_str += "{:<18}".format(obj_handle)
        
        # Part 3: root category
        obj_cat = obj_id.GetChildString('root-category')
        obj_str += "{:<18}".format(obj_cat)

        # Part 4: status predicates
        preds_id = obj_id.GetChildId('predicates')
        for sp in status_preds:
            val = preds_id.GetChildString(sp[0])
            is_true = (val is not None and val == sp[1])
            obj_str += "|{:^5}".format('X' if is_true else ' ')

        # Part 5: other predicates
        preds = [ pred for prop, pred in preds_id.GetAllChildWmes() if prop not in ignore_preds ]
        obj_str += '| ' + ' '.join(preds)
        return_str += obj_str + '\n'

    return_str += '\nRELATIONS:\n'

    rels_id = world_id.GetChildId('predicates')
    rel_ids = rels_id.GetAllChildIds('predicate')
    for rel_id in rel_ids:
        rel_handle = rel_id.GetChildString('handle')
        return_str += rel_handle + '\n'
        for ins in rel_id.GetAllChildIds('instance'):
            obj1_cat = ins.GetChildId('1').GetChildString('root-category')
            obj2_cat = ins.GetChildId('2').GetChildString('root-category')
            return_str += "  {}({}, {})\n".format(rel_handle, obj1_cat, obj2_cat)

    return return_str

