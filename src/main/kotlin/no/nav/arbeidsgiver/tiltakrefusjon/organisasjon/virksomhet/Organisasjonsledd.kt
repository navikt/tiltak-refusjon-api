data class Organisasjonsledd (
	val organisasjonsnummer : Int,
	val type : String,
	val navn : Navn,
	val inngaarIJuridiskEnheter : List<InngaarIJuridiskEnheter> = emptyList(),
	val organisasjonsleddOver : List<OrganisasjonsleddOver> = emptyList()
)