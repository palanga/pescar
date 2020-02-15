package gob.datos.consumer

object constants {

  object Path {

    /**
     * CSV with data on sea catch landings by location, fleet and species for the period
     * January 2010 to December 2018.
     *
     * 5489330 bytes
     * 41340 lines (excluding header)
     */
    val CAPTURA_PUERTO_FLOTA_2010_2018 = "/Users/palan/Downloads/captura-puerto-flota-2010-2018-utf8.csv"

    /**
     * The same as [[CAPTURA_PUERTO_FLOTA_2010_2018]] but with errors in some columns. Used for testing.
     */
    val CAPTURA_PUERTO_FLOTA_2010_2018_ERRORS =
      "/Users/palan/Downloads/captura-puerto-flota-2010-2018-utf8-with-errors.csv"

  }

  object Url {
    val DATOS_AGROINDUSTRIA_GOB_AR = "https://datos.agroindustria.gob.ar/api/3/action/datastore_search"
  }

  object ResourceId {

    /**
     * Data on sea catch landings by location, fleet and species for the period January 2010 to December 2018.
     */
    val DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2010_A_2018 = "1996a5ec-7075-4062-9a79-05868fc2a2e2"

    /**
     * Data on sea catch landings by location, fleet and species for the period January 2019 to present.
     */
    val DESEMBARQUE_DE_CAPTURA_DE_ESPECIES_MARÍTIMAS_2019 = "77a15b4a-71e1-4b81-9732-ae0b6863c8cc"

  }

}
