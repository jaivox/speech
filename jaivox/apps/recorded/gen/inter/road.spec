
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
JJ-N: slow, congested, busy
RB: fast, slow
RBR: quite fast, quite slow
RBS: very fast, very slow
}

{
smooth
type: attribute
fields: road
JJ-P: smooth, easy, nice
JJ-N: rough, busy, stop and go
RB: smoothly
RBR: more smooth
RBS: most smooth
}

