data class Forretningsadresser (

	val type : String,
	val adresselinje1 : String? = ".",
	val postnummer : Int,
	val landkode : String,
	val kommunenummer : Int,
	val bruksperiode : Bruksperiode,
	val gyldighetsperiode : Gyldighetsperiode
)