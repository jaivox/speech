
{
road.txt
type: table
columns: road, fast, smooth
}

{
road
type: field
attributes: fast, smooth
WP: welche
WP-M: welcher
ELS: sonst
ELS-JJ: andere
ELS-JJM: anderer
NN: Straße, Autobahn, Hauptstraße, Schnellstraße, Kraftfahrstraße
NNS: Straßen, Autobahnen, Hauptstraßen, Schnellstraßen, Kraftfahrstraßen
NN-M: Weg
NNS-M: Wege
NNP: [road.txt 0]
}

{
fast
type: attribute
fields: road
JJ-P: schnell
JJM-P: schneller
JJE-P: schnelle
JJR-P: schneller
JJS-P: schnellste
JJ-N: langsam, verstopft
JJM-N: langsamer, verstopfter
JJE-N: langsame, verstopfte
JJR-N: langsamer, verstopfter
JJS-N: langsamste, verstopfteste
RB: schnell, langsam
RBR: ziemlich schnell, ziemlich langsam
RBS: sehr schnell, sehr langsam
}

{
smooth
type: attribute
fields: road
JJ-P: glatt, einfach, gut
JJR-P: glatter, einfacher, besser
JJS-P: glatteste, einfachste, beste
JJM-P: glatter, einfacher, guter
JJE-P: glatte, einfache, gute
JJ-N: uneben, verstopft, stockend
JJM-N: unebener, verstopfter, stockender
JJE-N: unebene, verstopfte, stockende
JJR-N: unebener, verstopfter, stockender
JJS-N: unebenste, verstopfteste, stockendste
RB: problemlos
RBR: problemloser
RBS: am problemlossten
}

