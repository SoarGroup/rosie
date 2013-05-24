This file contains notes on how refsoar works. This is all of LGSoar up until generate-predicates,
which involves the Soar rules in the output/ directory.

Basic flow:
- referents are identified based on the individual words, starting with the main
	verb and extending to its arguments and other things connected there
- discourse centers are determined from referents
- ideas are determined from centers
- predicates are built from ideas (in output code, not refsoar)

TODO: what is filtered or added at each step (ref->center, center->idea, idea->predicate)?
All refs get a center, the center adds a verb/first/second/third flag.

refsoar files:
global.soar
activate.soar
interpret.soar
realize.soar
promote.soar
anaphor.soar
clause.soar

refsoar files currently not used:
build.soar
identify.soar <- two referents to the same individual
linkage.soar <- allows processing multiple sentences
transfer.soar
antecedent.soar <- cross-sentence coreference


LGSoar terminology:
root: the main verb of the sentence. It's word index is stored on the state as ^root
ref: structures in state.refsets.refs
  A ref is ..?
  Each ref has a ^head (link to the word on the sentence). A ^rooted-in link indicates that it is the root for that sentence.
  Presumably, there is support for independent refsets for each sentence, but that isn't used for now. Each refsets.refs has 
  a ^count attribute, which corresponds to the state.count int that is incremented as each sentence is processed.

center: structures on state.centers
  A center is ..?
  A center structure contains a link to a ref structure. 
  A center can be blocked (^blocked? yes) or unblocked.
  Centers have ^kind flags, which is verb, first, second, or third. Root verbs get ^kind verb, centers for refs pointing
  to words for first-person pronouns (I me mine) get ^kind first, second-person (you yours) get ^kind second, everything
  else gets ^kind third.
idea
promotion

LGSoar operators:
find-root (global.soar)
  This operator fires in a sequence at the beginning. It essentially seeks left to right, finding the rightmost
  word that can be reached by a sequence of links belonging to a certain set, defined in the proposal rule
  (among other things, no direct object or prepositional phrase links are followed). 
  This word is the root (main verb) of the sentence.

give-root-ref (interpret.soar)
  Once the root is found, it gets a ref.

add-center (anaphor.soar)
  Rules in anaphor.soar detect the various cases for center (refs to verbs, first/second person pronouns) 
  
add-arg (interpret.soar)
	An arg is an augmentation on a ref, ... 
	Rules in interpret.soar detect certain kinds of links and propose to add arguments to refs. These are proposed in parallel with
	add-center, but have lower priority.
	Operator instantiations have these augmentations:
		^order, ^level-two-order: priority
		^aug: name of augmentation
		^arg: value of augmentation
		^kind << single constant single-side >>
			constant has a structure in the arg, either a word (for modes) or feature/word pair
			single has an arg that is a word identifier
			single-side is like single, but ???
    ^link: id of the link causing the add-arg
    ^main: id of the word structure for the ref 

    Single (and single-side and double) add-args, which have another word in the arg, 
		add a ref for that other word as a side effect. This appears to be how refs propogate.

new-idea (realize.soar)
	Each ref is "realized" as a new idea or a coref to an existing idea, this operator handles the
	first case.
coref (promote.soar)
	Realize a ref as a reference to an existing idea. Currently (April 2012) not used in BOLT stuff,
	since it is really a multi-sentence thing.

basic, feature (promote.soar)
	These copy augmentations from the refs to the model (ideas). Augmentations pointing from ref A 
	to ref B get a corresponding feature in idea A pointing to idea B (direct promotion), there
	is also a reverse pointer (called "aug") going from idea B to idea A (indirect promotion). 
	Features that aren't pointers to refs/ideas are also copied via the "feature" operator (e.g.,
	modes like "always").

