YAHOO.namespace ("AnnoCultor");
YAHOO.AnnoCultor.Data = 
{"environment":[{"id":"annoCultorHome","value":"D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek"},{"id":"annoCultorHomeSource","value":"environment variable ANNOCULTOR_HOME"},{"id":"collectionDir","value":"D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek"},{"id":"collectionDirSource","value":"environment variable ANNOCULTOR_COLLECTION"},{"id":"diffDir","value":"D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek\\diff"},{"id":"docDir","value":"doc"},{"id":"previousDir","value":"D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek\\prev"},{"id":"tmpDir","value":"../tmp"},{"id":"outputOntologyDir","value":""},{"id":"localProfileFile","value":null},{"id":"keepPrevious","value":"false"},{"id":"inputDir","value":"input_source"},{"id":"outputDir","value":"output_rdf"},{"id":"vocabularyDir","value":"vocabularies"},{"id":"modelPerson","value":"http://annocultor.sourceforge.net/model/Person"},{"id":"modelPersonName","value":"http://annocultor.sourceforge.net/model/person.name"},{"id":"modelPersonBirthDate","value":"http://annocultor.sourceforge.net/model/person.birth.date"},{"id":"modelPersonDeathDate","value":"http://annocultor.sourceforge.net/model/person.death.date"}],"graphs":[{"id":"ak_ak.Links","subjects":2676,"properties":["http://www.vraweb.org/vracore/vracore3#location.creationSite","http://purl.org/dc/elements/1.1/subject","http://purl.org/dc/elements/1.1/material"],"triples":23422,"diff":"file://D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek\\diff\\ak_ak.Links.html"},{"id":"ak_ak.Works","subjects":2676,"properties":["http://www.vraweb.org/vracore/vracore3#culture","http://purl.org/dc/elements/1.1/Organisation","http://www.vraweb.org/vracore/vracore3#material.note","http://purl.org/dc/elements/1.1/ExtendedDescription","http://purl.org/dc/elements/1.1/identifier","http://purl.org/dc/elements/1.1/Description","http://purl.org/dc/elements/1.1/Style","http://purl.org/dc/elements/1.1/title","http://purl.org/dc/elements/1.1/Image","http://www.vraweb.org/vracore/vracore3#inscription","http://purl.org/dc/elements/1.1/Size","http://purl.org/dc/elements/1.1/TimePeriod"],"triples":27493,"diff":"file://D:\\eculture\\annocultor-2.x\\demos\\converters\\aziatischekeramiek\\diff\\ak_ak.Works.html"}],"rules":[{"id":"ConvertObject","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>ObjectRuleImpl<\/abbr>","tag":"fileset/file/records/object","firings":0},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/CulturalOrigin","firings":1948},{"id":"Wrapped VocabularyOfTerms","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Material","firings":2622},{"id":"RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>RenameLiteralPropertyRule<\/abbr>","tag":"fileset/file/records/object/id","firings":5352},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/TimePeriod","firings":2612},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/MaterialDescription","firings":1702},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Style","firings":2509},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Organisation","firings":2676},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Size","firings":2589},{"id":"VocabularyOfPlaces","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.lookuprules'>LookupPlaceRule<\/abbr>","tag":"fileset/file/records/object/GeographicalOrigin","firings":7655},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/TextSigns","firings":1124},{"id":"RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>RenameLiteralPropertyRule<\/abbr>","tag":"fileset/file/records/object/Name","firings":5298},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Image","firings":2676},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/ExtendedDescription","firings":193},{"id":"Wrapped VocabularyOfTerms","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Keyword","firings":2669},{"id":"Wrapped RenameProperty","rule":"<abbr class='namespaceAbbr' title='nl.multimedian.eculture.annocultor.core.rules'>WrappedRule<\/abbr>","tag":"fileset/file/records/object/Description","firings":2313}],"unusedtags":[{"id":"fileset/file/records/object/ExtendedDescription/sup","occurrences":1},{"id":"fileset/file/records/object/ExtendedDescription/table[@width]","occurrences":2},{"id":"fileset/file/records/object/Description/i","occurrences":1},{"id":"fileset/file/records/object/Description/p","occurrences":312},{"id":"fileset/file/records/object/ExtendedDescription/p/br","occurrences":16},{"id":"fileset/file/records/object/ExtendedDescription/table/tbody/tr/td[@width]","occurrences":2},{"id":"fileset/file[@name]","occurrences":4761},{"id":"fileset/file/records/object/Collection","occurrences":949},{"id":"fileset/file/records/object/ExtendedDescription/em","occurrences":18},{"id":"fileset/file/records/object/ExtendedDescription/p/sup","occurrences":3},{"id":"fileset/file/records/object/ExtendedDescription/table/tbody/tr/td[@valign]","occurrences":2},{"id":"fileset/file/records/object/Origin","occurrences":223},{"id":"fileset/file/records/object/ExtendedDescription/table/tbody/tr/td/p/br","occurrences":1},{"id":"fileset/file/records/object/Description/p/em","occurrences":10},{"id":"fileset/file/records/object/Description/br","occurrences":65},{"id":"fileset/file/records/object/Description/em","occurrences":14},{"id":"fileset/file/records/object/Description/p/br","occurrences":36},{"id":"fileset/file/records/object/ExtendedDescription/table[@cellspacing]","occurrences":2},{"id":"fileset/file/records/object/ExtendedDescription/p","occurrences":55},{"id":"fileset/file/records/object/Number","occurrences":2669},{"id":"fileset/file/records/object/ExtendedDescription/table/tbody/tr/td/p","occurrences":1},{"id":"fileset/file/records/object/Literature","occurrences":363},{"id":"fileset/file/records/object/ExtendedDescription/br","occurrences":21},{"id":"fileset/file/records/object/ExtendedDescription/table[@cellpadding]","occurrences":2},{"id":"fileset/file/records/object/ExtendedDescription/i","occurrences":1},{"id":"fileset/file/records/object/ExtendedDescription/p/em","occurrences":2}],"console":[{"line":"See console for console messages."}],"counters":[{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin::<b>ak_ak<\/b>","count":1},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>Japan<\/b>","count":368},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>Nederland (Delft)<\/b>","count":30},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>China (Jingdezhen)<\/b>","count":29},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>China (zuid)<\/b>","count":3},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>Longquan)<\/b>","count":2},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>China (Henan)<\/b>","count":2},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>China (Zhejiang<\/b>","count":2},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>China (Henan<\/b>","count":1},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>Qingliangsi)<\/b>","count":1},{"term":"LookupPlaceRule:fileset/file/records/object/GeographicalOrigin:no-voc-match:<b>Frankrijk<\/b>","count":1},{"term":"WrappedRule:fileset/file/records/object/Material::<b>ak_ak<\/b>","count":2},{"term":"RenameLiteralPropertyRule:fileset/file/records/object/Name::<b>ak_ak<\/b>","count":2},{"term":"ObjectRuleImpl:fileset/file/records/object::<b>Passed records<\/b>","count":2676},{"term":"ObjectRuleImpl:fileset/file/records/object::<b>Total records<\/b>","count":2676},{"term":"WrappedRule:fileset/file/records/object/Description::<b>ak_ak<\/b>","count":10},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Onderglazuur<\/b>","count":959},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Age Looxma Ypey<\/b>","count":950},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Wapenporselein<\/b>","count":239},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>IJzerrood<\/b>","count":193},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ming<\/b>","count":168},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Glazuur<\/b>","count":133},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Imari porselein<\/b>","count":131},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Chine de commande<\/b>","count":126},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Opglazuur<\/b>","count":122},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bovenglazuur<\/b>","count":122},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Servetwerk<\/b>","count":89},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Transitionporselein<\/b>","count":83},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bamboe<\/b>","count":66},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jingdezhen<\/b>","count":65},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Encre de Chine<\/b>","count":58},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Lijs<\/b>","count":40},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Lotusbloem<\/b>","count":40},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Strikwerk<\/b>","count":39},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Witte leeuw<\/b>","count":35},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vrouw (Europees)<\/b>","count":34},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kakiemon porselein<\/b>","count":33},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Fenghuang<\/b>","count":32},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pronk<\/b>","count":30},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Qing<\/b>","count":28},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Karakter<\/b>","count":28},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Scherf<\/b>","count":27},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Alsemblad<\/b>","count":27},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Parel<\/b>","count":26},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Draken<\/b>","count":22},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jade<\/b>","count":21},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Eend<\/b>","count":21},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kronen<\/b>","count":21},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Man (Europees)<\/b>","count":21},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Cartouche<\/b>","count":20},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Landschap<\/b>","count":20},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Inscriptie<\/b>","count":19},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pagode<\/b>","count":18},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Koperrood<\/b>","count":17},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Schip<\/b>","count":17},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Initiaal<\/b>","count":17},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Zandkorrels<\/b>","count":16},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Qilin<\/b>","count":16},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Twee vissen<\/b>","count":15},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tang dynastie<\/b>","count":15},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Acht kostbaarheden<\/b>","count":14},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Engel<\/b>","count":14},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Keramiek<\/b>","count":13},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Perziken<\/b>","count":13},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Zotje<\/b>","count":13},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Mensen<\/b>","count":13},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Zegelmerk<\/b>","count":13},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Paar<\/b>","count":12},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Monogram<\/b>","count":12},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Blanc de Chine<\/b>","count":11},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vis<\/b>","count":11},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Hond<\/b>","count":11},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Christus<\/b>","count":11},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Song dynastie<\/b>","count":10},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Huis<\/b>","count":10},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Gendi<\/b>","count":9},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Arita<\/b>","count":9},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tuin<\/b>","count":9},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Haas<\/b>","count":9},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Waterlandschap<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Soldaat<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kanton<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Yuan dynastie<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kobaltblauw<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Acht boeddhistische symbolen<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Powder blue<\/b>","count":8},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vrouw (Chinees)<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pad<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Samson<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Cavetto<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Amsterdams bont<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Sak�pot<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Koude beschildering<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Parasol<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Mandarijn<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Wolkenmotief<\/b>","count":7},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Han<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bediende<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Interieur<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vlag<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Maria<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vier kostbaarheden<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tafelberg<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Qin<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kerk<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Patipan<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Haven<\/b>","count":6},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Zhuanshu<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Insect<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kutani<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Prieel<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Acht onsterfelijken<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Boom<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Knobbelfles<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Theeceremonie<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Man (Chinees)<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bloem<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kruisiging<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Graf<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Biscuit<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Baoyue ping<\/b>","count":5},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Fort<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Johannes<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Karper<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Opstanding<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Rots<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kikkerdril<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vrucht<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Hengselmand<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Shunzhi<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Longquan<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Klassiek gewaad<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Leeuw<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Masker<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geestelijke<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vlinder<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Dorp<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Harlekijn<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tafel<\/b>","count":4},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Lambrequin<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pijp<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Coccejus<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Boek<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Venus<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jaargetijden<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ladder<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Herder<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Drie vrienden<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Shoulao<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Henan<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Nianhao<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Famille<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Wapenschild<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Gebroken ijs<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Emaillekleuren<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Schaap<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Godin<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jun<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Wagen<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Meerman<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vogel<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ooievaarsmerk<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kade<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Juno<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Neptunus<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Duif<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kind<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Putto<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>guirlandes<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jaartal<\/b>","count":3},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Diederik<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Gezellin<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kruis<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bos<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Berg<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geldermalsen<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Koopman<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>madame de<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Amor<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Misdadiger<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Budai<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>�tag�re<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Rivier<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geelvinck<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Monteith<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Schild<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Craquel�<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Muziekinstrument<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ezel<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bazuin<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Durven<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Wolk<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Desjima<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Maintenon<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tulpenvaas<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pioen<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Lodewijk XIV<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Caf� au lait<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tafelbaai<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Laotse<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Danseres<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Koepel<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Paris<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jozef<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Leda<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Rozet<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vingercitroen<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Merken<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Nis<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kasteel<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Chattermarks<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Satsuma (aardewerk)<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Altaar<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Gong<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Nanking cargo<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kribbe<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Stier<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Lieve<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Zwaan<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Temmoku (algemeen)<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Guanyin<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geboorte<\/b>","count":2},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>imari<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Manen<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Visnet<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Li Taibai<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>nis<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Hollandse vrijheidsmaagd<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vlieger<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Juweel<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Leeuwen<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Decollet�<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Vissertje<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geit<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Boeddhisme<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Poel<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Rebecca<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Pijl<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Winkel<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Geweer<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Draperie<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Thee<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Minerva<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Draak<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Emmer<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Tocht naar de rode wand<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Anhua<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jan<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Fruitmand<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kooi<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Hoorn<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Porselein\n      \n      ajourwerk<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ceres<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Devil\u2019s Peak<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Aphrodite<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Leeuwenhoek<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Schelpornament<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Jupiter<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Johanna Jacoba<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Ignatius van<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Kaolien<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Graafland<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Bron<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Master of the rocks<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Symbool<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Cupido<\/b>","count":1},{"term":"LookupTermRule:fileset/file/records/object/Keyword:no-voc-match:<b>Leyden<\/b>","count":1}]};
