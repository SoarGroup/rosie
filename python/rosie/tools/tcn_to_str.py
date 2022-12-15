
# Given the root LTI of a task concept network, 
#   returns a nicely formatted string with the task, subtasks, and goal-graph
def tcn_to_str(tcn_id, print_ltis=True):
    SlotMap.clear()
    return str(TCN(tcn_id, print_ltis))


################### INTERNAL STUFF #########################


# Helper methods
is_id = lambda id: hasattr(id, 'GetIdentifierSymbol')
has_child = lambda id, attr: (id.GetChildString(attr) is not None)

class TCN:
    def __init__(self, lti, print_ltis=True):
        SlotMap.clear()
        self.lti = lti
        self.print_ltis = print_ltis
        self.handle = lti.GetChildString('handle')
        self.task = TaskOperator(lti.GetChildId('procedural')) if has_child(lti, 'procedural') else None
        self.add_subtasks()
        self.add_goal_graph()
        for slot in SlotMap.slots.values():
            slot.implicit = False

    def add_subtasks(self):
        self.subtasks = {}
        proc_id = self.lti.GetChildId('procedural')
        if proc_id is None or not has_child(proc_id, 'subtasks'):
            return
        for subtask in sorted(proc_id.GetChildId('subtasks').GetAllChildIds('subtask')):
            self.subtasks[subtask.GetChildString('handle')] = TaskOperator(subtask)

    def add_goal_graph(self):
        self.goal_nodes = { }
        self.root_node = self.add_goal_node(self.lti.GetChildId('goal-graph'))

    def add_goal_node(self, node_id):
        if node_id is None:
            return None
        node_sym = node_id.GetIdentifierSymbol()
        if node_sym in self.goal_nodes:
            return self.goal_nodes[node_sym]
        node = GoalNode(node_id, self.subtasks, self.print_ltis)
        self.goal_nodes[node_sym] = node
        for next_id in node_id.GetAllChildIds('next'):
            tail = self.add_goal_node(next_id.GetChildId('goal'))
            node.add_edge(GoalEdge(node, next_id, tail))
        return node

    def __str__(self):
        s = "############################################################################"
        s = "Task Concept Network for {} {}\n\n".format(self.handle, \
                (self.lti.GetIdentifierSymbol() if self.print_ltis else ''))

        if self.task is not None:
            s += "Task Structure:\n"
            s += str(self.task)
            s += '\n'

        s += "Subtasks:\n\n"
        for subtask in self.subtasks.values():
            s += "  {} {}\n".format(subtask.lti.GetChildString('handle'), \
                (subtask.lti.GetIdentifierSymbol() if self.print_ltis else ''))
            s += str(subtask)
            s += '\n'

        s += "Goal Graph:\n\n"
        if self.root_node is not None:
            # Do a BFS through the graph and print each node
            visited = set()
            stack = [ self.root_node ]
            while len(stack) > 0:
                node = stack[0]
                stack = stack[1:]
                if node.symbol in visited:
                    continue
                visited.add(node.symbol)
                s += str(node)
                s += '\n'
                for edge in node.edges:
                    stack.append(edge.tail)

        return s


class TaskOperator:
    print_order = ["conditions", "modifiers", "arg1", "arg2", "arg3", "start-clause", "end-clause", "when-clause"]

    def __init__(self, lti):
        self.lti = lti
        self.name = lti.GetChildString('op_name')
        self.args = {}
        for wme in lti.GetAllChildWmes():
            if is_id(wme[1]) and has_child(wme[1], 'arg-type'):
                self.args[wme[0]] = make_argument(wme[0], wme[1])

    def __str__(self):
        s = "  {}("
        pargs = []
        # Want to list the arguments in a specific way
        for a in [ 'conditions', 'modifiers']:
            if a in self.args:
                pargs.append(str(self.args[a]))
        for a in [ 'arg1', 'arg2', 'arg3']:
            if a in self.args:
                pargs.append(a + ":" + str(self.args[a]))
        for a in ['start-clause', 'end-clause', 'when-clause']:
            if a in self.args:
                pargs.append(str(self.args[a]))
        s = "  {}({})\n".format(self.name, ', '.join(pargs))
        return s

################### GOAL NODES #########################

class GoalNode:
    def __init__(self, lti, subtask_map, print_ltis=True):
        self.print_ltis = print_ltis
        self.symbol = lti.GetIdentifierSymbol()
        self.handle = lti.GetChildString('handle')
        self.type = lti.GetChildString('item-type')
        self.preds = PredicateSet('', lti) if self.type == 'task-goal' else None
        if self.preds is not None:
            for pred in self.preds.preds:
                if isinstance(pred, Subtask):
                    subtask = next((sub for sub in subtask_map.values() if sub.lti.GetChildString('handle') == pred.handle), None)
                    pred.task_name = "" if subtask is None else subtask.name
        self.edges = []
    def add_edge(self, edge):
        self.edges.append(edge)
    def __str__(self):
        s = "  {}{}: {}\n".format(self.handle, ((' ' + self.symbol) if self.print_ltis else ''), \
                str(self.preds) if self.preds is not None else self.type)
        for edge in self.edges:
            s += "    {}\n".format(str(edge))
        return s

class GoalEdge:
    def __init__(self, head, edge, tail):
        self.head = head
        self.edge = edge
        self.tail = tail
        conditions = edge.GetChildId('conditions')
        self.conds = None if conditions is None else PredicateSet('', conditions)
    def __str__(self):
        if self.conds is None:
            return "--> {}".format(self.tail.handle)
        else:
            return "--IF{}-> {}".format(self.conds, self.tail.handle)


################### ARGUMENTS #########################

def make_argument(arg_name, arg_id):
    arg_type = arg_id.GetChildString('arg-type')
    if arg_type == 'object':            return ObjectArg(arg_id)
    if arg_type == 'partial-predicate': return PartialPredicate(arg_id)
    if arg_type == 'waypoint':          return SingularArg(arg_id)
    if arg_type == 'concept':           return SingularArg(arg_id)
    if arg_type == 'coordinate':        return SingularArg(arg_id)
    if arg_type == 'modifiers':         return Modifiers(arg_id)
    if arg_type == 'conditions':        return PredicateSet('if', arg_id)
    if arg_type == 'temporal-clause':   return PredicateSet(arg_name.split('-')[0], arg_id)
    print("UNKNOWN ARGUMENT: " + arg_type)
    return None

class ObjectArg:
    def __init__(self, lti):
        self.obj = SlotMap.get_slot(ObjectSlot, lti.GetChildId('id'))
    def __str__(self):
        return "{}".format(self.obj)

class PartialPredicate:
    def __init__(self, lti):
        self.pred = SlotMap.get_slot(PredicateSlot, lti.GetChildId('id'))
        self.obj2 = SlotMap.get_slot(ObjectSlot, lti.GetChildId('2'))
    def __str__(self):
        return "{}({})".format(self.pred, self.obj2)

class SingularArg:
    def __init__(self, lti):
        self.slot = SlotMap.get_slot(ConceptSlot, lti.GetChildId('id'))
    def __str__(self):
        return "{}".format(self.slot)


class Modifiers:
    def __init__(self, lti):
        self.mods = lti.GetAllChildValues('mod-handle')
    def __str__(self):
        return ", ".join("mod:" + m for m in self.mods)

class PredicateSet:
    def __init__(self, name, lti):
        self.name = name
        self.num_preds = lti.GetChildInt('pred-count')
        self.preds = [ make_predicate(lti.GetChildId(str(i+1))) for i in range(self.num_preds) ]
    def __str__(self):
        return "{}{{ {} }}".format(self.name, ", ".join(str(p) for p in self.preds))

################### PREDICATES #########################

def make_predicate(pred_id):
    pred_type = pred_id.GetChildString('type')
    if pred_type == 'unary':          return Unary(pred_id)
    if pred_type == 'relation':       return Relation(pred_id)
    if pred_type == 'clocktime':      return Clocktime(pred_id)
    if pred_type == 'duration':       return Duration(pred_id)
    if pred_type == 'object-exists':  return Exists(pred_id)
    if pred_type == 'object-missing': return Missing(pred_id)
    if pred_type == 'subtask':        return Subtask(pred_id)
    if pred_type == 'status':         return Status(pred_id)
    print("UNKNOWN PREDICATE: " + pred_type)
    return None

class Unary:
    def __init__(self, lti):
        self.pred = SlotMap.get_slot(PredicateSlot, lti.GetChildId('id'))
        self.obj1 = SlotMap.get_slot(ObjectSlot, lti.GetChildId('1'))
    def __str__(self):
        return "{}({})".format(self.pred, self.obj1)


class Relation:
    def __init__(self, lti):
        self.pred = SlotMap.get_slot(PredicateSlot, lti.GetChildId('id'))
        self.obj1 = SlotMap.get_slot(ObjectSlot, lti.GetChildId('1'))
        self.obj2 = SlotMap.get_slot(ObjectSlot, lti.GetChildId('2'))
    def __str__(self):
        return "{}({},{})".format(self.pred, self.obj1, self.obj2)

class Clocktime:
    def __init__(self, lti):
        self.hour = lti.GetChildInt('hour')
        self.minute = lti.GetChildInt('minute')
    def __str__(self):
        return "{}:{}".format(self.hour, self.minute)

class Duration:
    def __init__(self, lti):
        self.number = lti.GetChildInt('number')
        self.unit = lti.GetChildString('unit')
    def __str__(self):
        return "{} {}".format(self.number, self.unit)

class Exists:
    def __init__(self, lti):
        self.preds = [ wme[1].GetChildString('predicate-handle') for wme in lti.GetAllChildWmes() if is_id(wme[1]) ]
    def __str__(self):
        return "exists[{}]".format(','.join(self.preds))

class Missing:
    def __init__(self, lti):
        self.preds = [ wme[1].GetChildString('predicate-handle') for wme in lti.GetAllChildWmes() if is_id(wme[1]) ]
    def __str__(self):
        return "missing[{}]".format(','.join(self.preds))

class Subtask:
    def __init__(self, lti):
        self.handle = lti.GetChildString('subtask-handle')
        self.task_name = ""
    def __str__(self):
        return "exec({}:{})".format(self.handle, self.task_name)

class Status:
    def __init__(self, lti):
        self.type = lti.GetChildString('name')
        if self.type == 'current-location':
            self.loc = SlotMap.get_slot(ObjectSlot, lti.GetChildId('1'))
        else:
            self.loc = None
    def __str__(self):
        if self.loc is not None:
            return "cur-loc({})".format(self.loc)
        return self.type

################### SLOTS #########################

class SlotMap:
    slots = {}
    def clear():
        SlotMap.slots = {}

    def get_slot(cls, lti):
        sym = lti.GetIdentifierSymbol()
        if sym not in SlotMap.slots:
            SlotMap.slots[sym] = cls(lti, len(SlotMap.slots)+1)
        SlotMap.slots[sym].count += 1
        return SlotMap.slots[sym]

class Slot:
    def __init__(self, lti, num):
        self.num = num
        self.count = 0
        self.default = lti.GetChildId('default')
        self.implicit = True
    def __str__(self):
        if self.implicit or self.count == 1:
            return self.get_default()
        return '<S' + str(self.num) + '>' + self.get_default()

class ConceptSlot(Slot):
    def get_default(self):
        return ''

class ObjectSlot(Slot):
    def get_default(self):
        if self.default is None:
            return ''
        preds_id = self.default.GetChildId('predicates')
        return '[' + ','.join(wme[1] for wme in preds_id.GetAllChildWmes()) + ']'

class PredicateSlot(Slot):
    def get_default(self):
        if self.default is None or self.default.GetChildString('predicate-handle') is None:
            return ''
        return self.default.GetChildString('predicate-handle')

### For testing
#
#example_tcn = \
#"""
#(@100000 ^goal-graph @1587 ^handle pick-up1 ^item-type action ^procedural @1588 [+0.000])
#  (@1587 ^handle pick-up1start1 ^item-type start-goal ^next @1593 [+0.000])
#  (@1588 ^arg1 @1589 ^op_name op_pick-up1 ^subtasks @1590 [+0.000])
#    (@1593 ^goal @1592 [+0.000])
#    (@1589 ^arg-type object ^id @1591 ^required true [+0.000])
#    (@1590 [+0.000])
#      (@1592 ^1 @1594 ^handle pick-up1goal1 ^item-type task-goal ^next @1595 ^pred-count 1 [+0.000])
#      (@1591 [+0.000])
#        (@1594 ^1 @1591 ^id @1597 ^type unary [+0.000])
#        (@1595 ^goal @1596 [+0.000])
#          (@1597 ^default @1598 [+0.000])
#          (@1596 ^handle pick-up1term1 ^item-type terminal-goal [+0.000])
#            (@1598 ^predicate-handle grabbed1 ^property-handle is-grabbed1 [+0.000])
#"""
#
#from pysoarlib.util import PrintoutIdentifier, parse_wm_printout
#tcn_id = PrintoutIdentifier(parse_wm_printout(example_tcn), "@100000")
#print(tcn_to_str(tcn_id, print_ltis=True))
    
