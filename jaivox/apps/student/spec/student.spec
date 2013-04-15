{
student.txt
type: table
columns: student, detailed, frequent
}

{
student
type: field
attributes: detailed, frequent
WP: who
ELS: other, besides, else
NN: student, kid, scholar
NNS: students, kids, scholars
NNP: [student.txt  0]
}

{
detailed
type: attribute
fields: student
JJ-P: detailed, extensive, long
JJ-N: brief, short
JJ-P: fast, quick
JJR-P: more detailed, more extensive, longer
JJS-P: most detailed, most extensive, longest
JJR-N: more brief, shorter
JJS-N: most brief, shortest
RB: detailed
RBR: very detailed
RBS: most detailed
}

{
frequent
type: attribute
fields: student
JJ-P: frequent, steady, common
JJ-N: infrequent
JJR-P: more frequent, steadier, more common
JJS-P: most frequent, steadiest, most common
JJR-N: more infrequent
JJS-N: most infrequent
RB: usually, often
RBR: very often
RBS: most often
}

