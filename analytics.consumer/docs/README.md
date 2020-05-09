
* download csv data:

captura puerto flota 2010-2018:
`https://datos.agroindustria.gob.ar/dataset/e2f12522-4dea-495e-877a-b6d2737ae6bf/resource/1996a5ec-7075-4062-9a79-05868fc2a2e2/download/captura-puerto-flota-2010-2018.csv`

captura puerto flota 2019:
`https://datos.agroindustria.gob.ar/dataset/e2f12522-4dea-495e-877a-b6d2737ae6bf/resource/77a15b4a-71e1-4b81-9732-ae0b6863c8cc/download/captura-puerto-flota-2019.csv`


* convert to utf-8:

first get the current encoding:
```bash
$ file -I captura-puerto-flota-2010-2018.csv
captura-puerto-flota-2010-2018.csv: text/plain; charset=iso-8859-1
```

then convert:
```bash
iconv -f ISO-8859-1 -t UTF-8 captura-puerto-flota-2010-2018.csv  > captura-puerto-flota-2010-2018-utf8.csv
```
where `ISO-8859-1` is the encoding we got from the previous command

