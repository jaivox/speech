
{
road.txt
type: table
columns: road, fast, smooth
}

{
road
type: field
attributes: fast, smooth
WP: qué, cuál
WPS: qué, cuáles
ELS: demás, además
NN-M: camino
NN-F: calle, ruta, carretera, autopista, vía
NNS-M: caminos
NNS-F: calles, rutas, carreteras, autopistas, vías
NNP: [road.txt 0]
}

{
fast
type: attribute
fields: road
JJM-P: rápido
JJF-P: rápida
JJM-PS: rápidos
JJF-PS: rápidas
JJM-N: lento, atestado, congestionado
JJF-N: lenta, atestada, congestionada
JJM-NS: lentos, atestados, congestionados 
JJF-NS: lentas, atestadas, congestionadas
RB: rápido, lento, despacio
RBR: muy rápido, muy lento, muy despacio
RBS: rapidísimo, lentísimo, despacísimo
}

{
smooth
type: attribute
fields: road
JJM-PB: buen
JJM-P: liso, fácil de transitar, bueno
JJF-P: lisa, fácil de transitar, buena
JJM-PS: lisos, fáciles de transitar, buenos
JJF-PS: lisas, fáciles de transitar, buenas
JJ-N: con mucho tráfico
JJM-N: áspero, intermitente
JJF-N: áspera, intermitente
JJM-NS: ásperos, intermitentes
JJF-NS: ásperas, intermitentes
RB: lisamente
RBR-M: más liso
RBR-F: más lisa
RBR-MS: más lisos
RBR-FS: más lisas
RBS-M: el más liso, lo más liso 
RBS-F: la más lisa
RBS-MS: los más lisos
RBS-FS: las más lisas
}

