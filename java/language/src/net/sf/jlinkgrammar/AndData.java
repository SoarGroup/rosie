package net.sf.jlinkgrammar;

/**
 *   
 *                               Notes about AND
 *  <p>
 *    A large fraction of the code of this parser seems to deal with handling
 *    conjunctions.  This comment (combined with reading the paper) should
 *    give an idea of how it works.
 *  <p>
 *    First of all, we need a more detailed discussion of strings, what they
 *    match, etc.  (This entire discussion ignores the labels, which are
 *    semantically the same as the leading upper case letters of the
 *    connector.)
 *  <p>
 *    We'll deal with infinite strings from an alphabet of three types of
 *    characters: "*". "^" and ordinary characters (denoted "a" and "b").
 *    (The end of a string should be thought of as an infinite sequence of
 *    "*"s).
 *  <p>
 *    Let match(s) be the set of strings that will match the string s.  This
 *    is defined as follows. A string t is in match(s) if (1) its leading
 *    upper case letters exactly match those of s.  (2) traversing through
 *    both strings, from left to right in step, no missmatch is found
 *    between corresponding letters.  A missmatch is a pair of differing
 *    ordinary characters, or a "^" and any ordinary letter or two "^"s.
 *    In other words, a match is exactly a "*" and anything, or two
 *    identical ordinary letters.
 *  <p>
 *    Alternative definition of the set match(s):
 *    {t | t is obtained from s by replacing each "^" and any other characters
 *    by "*"s, and replacing any original "*" in s by any other character
 *    (or "^").}
 *  <p>
 *    Theorem: if t in match(s) then s in match(t).
 *  <p>
 *    It is also a theorem that given any two strings s and t, there exists a
 *    unique new string u with the property that:
 *  <p>
 *            match(u) = match(s) intersect match(t)
 *  <p>
 *    This string is called the GCD of s and t.  Here are some examples.
 *  <ul>
 *  <li>          GCD(N*a,Nb) = Nba
 *  <li>          GCD(Na, Nb) = N^
 *  <li>          GCD(Nab,Nb) = N^b
 *  <li>          GCD(N^,N*a) = N^a
 *  <li>          GCD(N^,  N) = N^
 *  <li>          GCD(N^^,N^) = N^^
 *  </ul>
 *  <p>  
 *     We need an algorithm for computing the GCD of two strings.  Here is
 *    one.
 *   <p> 
 *    First get by the upper case letters (which must be equal, otherwise
 *    there is no intersection), issuing them.  Traverse the rest of the
 *    characters of s and t in lockstep until there is nothing left but
 *    "*"s.  If the two characters are:
 *  <p>
 * <ul>  
 *  <li>            "a" and "a", issue "a"
 *  <li>            "a" and "b", issue "^"
 *  <li>            "a" and "*", issue "a"
 *  <li>            "*" and "*", issue "*"
 *  <li>            "*" and "^", issue "^"
 *  <li>            "a" and "^", issue "^"
 *  <li>            "^" and "^", issue "^"
 * </ul> 
 *  <p> 
 *    A simple case analysis suffices to show that any string that matches
 *    the right side, must match both of the left sides, and any string not
 *    matching the right side must not match at least one of the left sides.
 *  <p>  
 *    This proves that the GCD operator is associative and commutative.
 *    (There must be a name for a mathematical structure with these properties.)
 *  <p>  
 *    To elaborate further on this theory, define the notion of two strings
 *    matching in the dual sense as follows: s and t dual-match if
 *    match(s) is contained in match(t) or vice versa---
 *  <p>  
 *    Full development of this theory could lead to a more efficient
 *    algorithm for this problem.  I'll defer this until such time as it
 *    appears necessary.
 *   <p> 
 *   <p> 
 *    We need a data structure that stores a set of fat links.  Each fat
 *    link has a number (called its label).  The fat link operates in liu of
 *    a collection of links.  The particular stuff it is a substitute for is
 *    defined by a disjunct.  This disjunct is stored in the data structure.
 *   <p> 
 *    The type of a disjunct is defined by the sequence of connector types
 *    (defined by their upper case letters) that comprises it.  Each entry
 *    of the label_table[] points to a list of disjuncts that have the same
 *    type (a hash table is uses so that, given a disjunct, we can efficiently
 *    compute the element of the label table in which it belongs).
 *  <p>  
 *    We begin by loading up the label table with all of the possible
 *    fat links that occur through the words of the sentence.  These are
 *    obtained by taking every sub-range of the connectors of each disjunct
 *    (containing the center).  We also compute the closure (under the GCD
 *    operator) of these disjuncts and store also store these in the
 *    label_table.  Each disjunct in this table has a string which represents
 *    the subscripts of all of its connectors (and their multi-connector bits).
 *  <p>  
 *    It is possible to generate a fat connector for any one of the
 *    disjuncts in the label_table.  This connector's label field is given
 *    the label from the disjunct from which it arose.  It's string field
 *    is taken from the string of the disjunct (mentioned above).  It will be
 *    given a priority with a value of UP_priority or DOWN_priority (depending
 *    on how it will be used).  A connector of UP_priority can match one of
 *    DOWN_priority, but neither of these can match any other priority.
 *    (Of course, a fat connector can match only another fat connector with
 *    the same label.)
 *  <p>  
 *    The paper describes in some detail how disjuncts are given to words
 *    and to "and" and ",", etc.  Each word in the sentence gets many more
 *    new disjuncts.  For each contiguous set of connectors containing (or
 *    adjacent to) the center of the disjunct, we generate a fat link, and
 *    replace these connector in the word by a fat link.  (Actually we do
 *    this twice.  Once pointing to the right, once to the left.)  These fat
 *    links have priority UP_priority.
 *  <p>  
 *    What do we generate for ","?  For each type of fat link (each label)
 *    we make a disjunct that has two down connectors (to the right and left)
 *    and one up connector (to the right).  There will be a unique way of
 *    hooking together a comma-separated and-list.
 *  <p>  
 *    The disjuncts on "and" are more complicated.  Here we have to do just what
 *    we did for comma (but also include the up link to the left), then
 *    we also have to allow the process to terminate.  So, there is a disjunct
 *    with two down fat links, and between them are the original thin links.
 *    These are said to "blossom" out.  However, this is not all that is
 *    necessary.  It's possible for an and-list to be part of another and list
 *    with a different labeled fat connector.  To make this possible, we
 *    regroup the just blossomed disjuncts (in all possible ways about the center)
 *    and install them as fat links.  If this sounds like a lot of disjuncts --
 *    it is!  The program is currently fairly slow on long sentence with and.
 *  <p>  
 *    It is slightly non-obvious that the fat-links in a linkage constructed
 *    from disjuncts defined in this way form a binary tree.  Naturally,
 *    connectors with UP_priority point up the tree, and those with DOWN_priority
 *    point down the tree.
 *  <p>  
 *    Think of the string x on the connector as representing a set X of strings.
 *    X = match(x).  So, for example, if x="S^" then match(x) = {"S", "S*a",
 *    "S*b", etc}.  The matching rules for UP and DOWN priority connectors
 *    are such that as you go up (the tree of ands) the X sets get no larger.
 *    So, for example, a "Sb" pointing up can match an "S^" pointing down.
 *    (Because more stuff can match "Sb" than can match "S^".)
 *    This guarantees that whatever connector ultimately gets used after the
 *    fat connector blossoms out (see below), it is a powerful enough connector
 *    to be able to match to any of the connectors associated with it.
 *  <p>  
 *    One problem with the scheme just descibed is that it sometimes generates
 *    essentially the same linkage several times.  This happens if there is
 *    a gap in the connective power, and the mismatch can be moved around in
 *    different ways.  Here is an example of how this happens.
 *   <p> 
 *    (Left is DOWN, right is UP)
 *  <p>
 * <ul>   
      *  <li>   Sa <--. S^ <--. S            or             Sa <--. Sa <--. S 
      *  <li>   fat      thin                                 fat      thin
 *  </ul>  
 *    Here two of the disjunct types are given by "S^" and "Sa".  Notice that
 *    the criterion of shrinking the matching set is satisfied by the the fat
 *    link (traversing from left to right).  How do I eliminate one of these?
 *   <p> 
 *    I use the technique of canonization.  I generate all the linkages.  There
 *    is then a procedure that can check to see of a linkage is canonical.
 *    If it is, it's used, otherwise it's ignored.  It's claimed that exactly
 *    one canonical one of each equivalence class will be generated.
 *    We basically insist that the intermediate fat disjuncts (ones that
 *    have a fat link pointing down) are all minimal -- that is, that they
 *    cannot be replaced by by another (with a strictly) smaller match set.
 *    If one is not minimal, then the linkage is rejected.
 *   <p> 
 *    Here's a proof that this is correct.  Consider the set of equivalent
 *    linkages that are generated.  These Pick a disjunct that is the root of
 *    its tree.  Consider the set of all disjuncts which occur in that positon
 *    among the equivalent linkages.  The GCD of all of these can fit in that
 *    position (it matches down the tree, since its match set has gotten
 *    smaller, and it also matches to the THIN links.)  Since the GCD is put
 *    on "and" this particular one will be generated.  Therefore rejecting
 *    a linkage in which a root fat disjunct can be replaced by a smaller one
 *    is ok (since the smaller one will be generated separately).  What about
 *    a fat disjunct that is not the root.  We consider the set of linkages in 
 *    which the root is minimal (the ones for which it's not have already been
 *    eliminated).  Now, consider one of the children of the root in precisely
 *    the way we just considered the root.  The same argument holds.  The only
 *    difference is that the root node gives another constraint on how small
 *    you can make the disjunct -- so, within these constraints, if we can go
 *    smaller, we reject.
 *   <p> 
 *    The code to do all of this is fairly ugly, but I think it works.
 *    <p>
 *  
 *  Problems with this stuff:
 *    <p>
 *    1) There is obviously a combinatorial explosion that takes place.
 *       As the number of disjuncts (and the number of their subscripts
 *       increase) the number of disjuncts that get put onto "and" will
 *       increase tremendously.  When we made the transcript for the tech
 *       report (Around August 1991) most of the sentence were processed
 *       in well under 10 seconds.  Now (Jan 1992), some of these sentences
 *       take ten times longer.  As of this writing I don't really know the
 *       reason, other than just the fact that the dictionary entries are
 *       more complex than they used to be.   The number of linkages has also
 *       increased significantly.
 *    <p>
 *    2) Each element of an and list must be attached through only one word.
 *       This disallows "there is time enough and space enough for both of us", 
 *       and many other reasonable sounding things.  The combinatorial
 *       explosion that would occur if you allowed two different connection
 *       points would be tremendous, and the number of solutions would also
 *       probably go up by another order of magnitude.  Perhaps if there
 *       were strong constraints on the type of connectors in which this
 *       would be allowed, then this would be a conceivable prospect.
 *   <p> 
 *    3) A multi-connector must be either all "outside" or all "inside" the and.
 *       For example, "the big black dog and cat ran" has only two ways to
 *       linkages (instead of three).
 *    <p>
 *  Possible bug: It seems that the following two linkages should be the
 *  same under the canonical linkage test.  Could this have to do with the
 *  pluralization system?
 *    <p>
 *  > I am big and the bike and the car were broken
 *  Accepted (4 linkages, 4 with no P.P. violations) at stage 1
 *    Linkage 1, cost vector = (0, 0, 18)
 * <ul>   
 *  <li>                                      +------Spx-----+      
 *  <li>          +-----CC-----+------Wd------+-d^^*i^-+     |      
 *  <li>     +-Wd-+Spi+-Pa+    |   +--Ds-+d^^*+   +-Ds-+     +--Pv-+
 *  <li>     |    |   |   |    |   |     |    |   |    |     |     |
 *  <li>   //    /// I.p am big.a and the bike.n and the car.n were broken 
 *  <li>   
 *  <li>          /////          RW      <---RW---.  RW        /////
 *  <li>          /////          Wd      <---Wd---.  Wd        I.p
 *  <li>          I.p            CC      <---CC---.  CC        and
 *  <li>          I.p            Sp*i    <---Spii-.  Spi       am
 *  <li>          am             Pa      <---Pa---.  Pa        big.a
 *  <li>          and            Wd      <---Wd---.  Wd        and
 *  <li>          bike.n         d^s**  6<---d^^*i.  d^^*i  6  and
 *  <li>          the            D       <---Ds---.  Ds        bike.n
 *  <li>          and            Sp      <---Spx--.  Spx       were
 *  <li>          and            d^^*i  6<---d^^*i.  d^s**  6  car.n
 *  <li>          the            D       <---Ds---.  Ds        car.n
 *  <li>          were           Pv      <---Pv---.  Pv        broken
 * </ul> 
 *  <p>
  * (press return for another)
 *  <p>
  * > 
 *  <p>
  *   Linkage 2, cost vector = (0, 0, 18)
  * <ul> 
  *  <li>                                     +------Spx-----+      
  *  <li>         +-----CC-----+------Wd------+-d^s**^-+     |      
  *  <li>    +-Wd-+Spi+-Pa+    |   +--Ds-+d^s*+   +-Ds-+     +--Pv-+
  *  <li>    |    |   |   |    |   |     |    |   |    |     |     |
  *  <li>   //    /// I.p am big.a and the bike.n and the car.n were broken 
  *  <li>  
  *  <li>         /////          RW      <---RW---.  RW        /////
  *  <li>         /////          Wd      <---Wd---.  Wd        I.p
  *  <li>         I.p            CC      <---CC---.  CC        and
  *  <li>         I.p            Sp*i    <---Spii-.  Spi       am
  *  <li>         am             Pa      <---Pa---.  Pa        big.a
  *  <li>         and            Wd      <---Wd---.  Wd        and
  *  <li>         bike.n         d^s**  6<---d^s**.  d^s**  6  and
  *  <li>         the            D       <---Ds---.  Ds        bike.n
  *  <li>         and            Sp      <---Spx--.  Spx       were
  *  <li>         and            d^s**  6<---d^s**.  d^s**  6  car.n
  *  <li>         the            D       <---Ds---.  Ds        car.n
  *  <li>         were           Pv      <---Pv---.  Pv        broken
  * </ul>  
    
 *
 */
public class AndData {
    int          LT_bound;
    int          LT_size;
    Disjunct   label_table[];
    LabelNode  hash_table[]=new LabelNode[GlobalBean.HT_SIZE];

}
