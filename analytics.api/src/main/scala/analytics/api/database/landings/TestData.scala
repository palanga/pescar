package analytics.api.database.landings

import java.time.YearMonth

import analytics.api.types.Metric.Landing
import analytics.api.types.{ Fleet, Location, Specie }
import utils.GeoLocation

object TestData {

  val LOCATIONS_ALL = Set(
    Location.Harbour("Almanza", GeoLocation.zero),
    Location.Harbour("Bahía Blanca", GeoLocation.zero),
    Location.Harbour("Caleta Cordova", GeoLocation.zero),
    Location.Harbour("Caleta Olivia / Paula", GeoLocation.zero),
    Location.Harbour("Camarones", GeoLocation.zero),
    Location.Harbour("Comodoro Rivadavia", GeoLocation.zero),
    Location.Harbour("General Lavalle", GeoLocation.zero),
    Location.Harbour("Mar del Plata", GeoLocation.zero),
    Location.Harbour("Necochea / Quequén", GeoLocation.zero),
    Location.Harbour("Puerto Deseado", GeoLocation.zero),
    Location.Harbour("Puerto Madryn", GeoLocation.zero),
    Location.Harbour("Punta Colorada", GeoLocation.zero),
    Location.Harbour("Punta Quilla", GeoLocation.zero),
    Location.Harbour("Rawson", GeoLocation.zero),
    Location.Harbour("Rosales", GeoLocation.zero),
    Location.Harbour("Río Salado", GeoLocation.zero),
    Location.Harbour("San Antonio Este", GeoLocation.zero),
    Location.Harbour("San Antonio Oeste", GeoLocation.zero),
    Location.Harbour("San Clemente del Tuyú", GeoLocation.zero),
    Location.Harbour("San Julián", GeoLocation.zero),
    Location.Harbour("Ushuaia", GeoLocation.zero),
    Location.Miscellaneous("otros puertos"),
    Location.Miscellaneous("otros puertos Buenos Aires"),
  )

  val SPECIES_ALL = Set(
    Specie("Abadejo"),
    Specie("Almejas nep"),
    Specie("Anchoa de banco"),
    Specie("Anchoíta"),
    Specie("Bacalao austral"),
    Specie("Bagre"),
    Specie("Besugo"),
    Specie("Bonito"),
    Specie("Brótola"),
    Specie("Caballa"),
    Specie("Cabrilla"),
    Specie("Calamar Illex"),
    Specie("Calamar Loligo"),
    Specie("Calamar patagónico"),
    Specie("Camarón"),
    Specie("Cangrejo"),
    Specie("Caracol"),
    Specie("Castañeta"),
    Specie("Cazón"),
    Specie("Centolla"),
    Specie("Centollón"),
    Specie("Chernia"),
    Specie("Cholga"),
    Specie("Chucho"),
    Specie("Congrio de profundidad"),
    Specie("Congrio"),
    Specie("Cornalito"),
    Specie("Corvina blanca"),
    Specie("Corvina negra"),
    Specie("Gatuzo"),
    Specie("Granadero"),
    Specie("Jurel"),
    Specie("Langostino"),
    Specie("Lenguados nep"),
    Specie("Lisa"),
    Specie("Lurión común"),
    Specie("Mejillón"),
    Specie("Merluza austral"),
    Specie("Merluza de cola"),
    Specie("Merluza hubbsi"),
    Specie("Merluza negra"),
    Specie("Mero"),
    Specie("Notothenia"),
    Specie("Otras - Algas, etc."),
    Specie("Otras especies de crustác"),
    Specie("Otras especies de molusco"),
    Specie("Otras especies de peces"),
    Specie("Palometa"),
    Specie("Pampanito"),
    Specie("Papafigo"),
    Specie("Pargo"),
    Specie("Pejerrey"),
    Specie("Pescadilla real"),
    Specie("Pescadilla"),
    Specie("Pez gallo"),
    Specie("Pez limón"),
    Specie("Pez palo"),
    Specie("Pez sable"),
    Specie("Pez ángel"),
    Specie("Polaca"),
    Specie("Pulpitos"),
    Specie("Pulpos nep"),
    Specie("Raya cola corta"),
    Specie("Raya de círculos"),
    Specie("Raya espinosa"),
    Specie("Raya hocicuda / picuda"),
    Specie("Raya lisa"),
    Specie("Raya marmolada"),
    Specie("Raya marrón oscuro"),
    Specie("Raya pintada"),
    Specie("Rayas nep"),
    Specie("Rubio"),
    Specie("Róbalo"),
    Specie("Salmonete"),
    Specie("Salmón de mar"),
    Specie("Saraca"),
    Specie("Sargo"),
    Specie("Savorín"),
    Specie("Tiburones nep"),
    Specie("Tiburón azul"),
    Specie("Tiburón bacota"),
    Specie("Tiburón escalandrún"),
    Specie("Tiburón espinoso"),
    Specie("Tiburón gris"),
    Specie("Tiburón martillo"),
    Specie("Tiburón moteado"),
    Specie("Tiburón peregrino"),
    Specie("Tiburón pintaroja"),
    Specie("Variado costero"),
    Specie("Vieira (callos)"),
  )

  val FLEETS_ALL = Set(
    Fleet("Congeladores arrastreros"),
    Fleet("Congeladores palangreros"),
    Fleet("Congeladores poteros nacionales"),
    Fleet("Congeladores tangoneros"),
    Fleet("Congeladores trampas"),
    Fleet("Costeros"),
    Fleet("Fresqueros"),
    Fleet("Rada o ría"),
  )

  private val MarDelPlata  = Location.Harbour("Mar del Plata", GeoLocation.zero)
  private val PuertoMadryn = Location.Harbour("Puerto Madryn", GeoLocation.zero)

  private val Langostino    = Specie("Langostino")
  private val MerluzaHubbsi = Specie("Merluza hubbsi")

  private val RadaORia   = Fleet("Rada o ría")
  private val Fresqueros = Fleet("Fresqueros")

  val LANDINGS_ALL = List(
    Landing(YearMonth.of(2017, 7), MarDelPlata, Langostino, RadaORia, 2),
    Landing(YearMonth.of(2017, 7), MarDelPlata, MerluzaHubbsi, RadaORia, 3),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, Langostino, RadaORia, 1),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, MerluzaHubbsi, Fresqueros, 1),
    Landing(YearMonth.of(2017, 8), MarDelPlata, Langostino, RadaORia, 10),
    Landing(YearMonth.of(2017, 8), MarDelPlata, MerluzaHubbsi, RadaORia, 7),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, Langostino, RadaORia, 9),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, MerluzaHubbsi, Fresqueros, 4),
  )

}
