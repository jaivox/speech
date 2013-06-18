
{
road.txt
type: table
columns: road, fast, smooth
}

{
road
type: field
attributes: fast, smooth
WP: what, which
ELS: other, besides, else
NN: road, route, highway, freeway
NNS: roads, routes, highways, freeways
NNP: [road.txt 0]
}

{
fast
type: attribute
fields: road
JJ-P: fast, quick
JJR-P: faster, quicker
JJS-P: fastest, quickest
JJ-N: slow, congested, busy
JJR-N: slower, more congested, more busy
JJS-N: slowest, most congested, busiest
RB: fast, slow
RBR: quite fast, quite slow
RBS: very fast, very slow
}

{
smooth
type: attribute
fields: road
JJ-P: smooth, easy, nice
JJR-P: smoother, easier, nicer
JJS-P: smoothest, easiest, nicest
JJ-N: rough, busy, stop and go
JJR-N: rougher, busier, more stop and go
JJS-N: roughest, busiest, most stop and go
RB: smoothly
RBR: more smooth
RBS: most smooth
}

