import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.Virksomhet

data class EregOrganisasjon (
	val organisasjonsnummer : Int,
	val type : String,
	val navn : Navn,
	val organisasjonDetaljer : OrganisasjonDetaljer,
	val inngaarIJuridiskEnheter : List<InngaarIJuridiskEnheter>? = emptyList(),
	val bestaarAvOrganisasjonsledd: List<BestaarAvOrganisasjonsledd>? = emptyList()
){
	fun tilDomeneObjekt(): Virksomhet {
		if(!harAdresseOgJuridiskOrgNr(inngaarIJuridiskEnheter, bestaarAvOrganisasjonsledd)) {
			throw FeilkodeException(Feilkode.EREG_MANGLER_ADRESSEINFO)
		}

			var juridiskEnhetNavnOgOrgNr = Pair("","")

			if(inngaarIJuridiskEnheter != null && inngaarIJuridiskEnheter.isNotEmpty()){
				juridiskEnhetNavnOgOrgNr = hentJuridiskEnhet(inngaarIJuridiskEnheter)
			}
			if(bestaarAvOrganisasjonsledd != null && bestaarAvOrganisasjonsledd.isNotEmpty()){
				juridiskEnhetNavnOgOrgNr = hentJuridiskEnhetFraOrganisasjonsledd(bestaarAvOrganisasjonsledd)
			}

			return Virksomhet(
				juridiskEnhetNavnOgOrgNr.second,
				organisasjonsnummer.toString(),
				organisasjonDetaljer.forretningsadresser.first().adresselinje1 ?: ".",
				organisasjonDetaljer.forretningsadresser.first().postnummer.toString(),
				juridiskEnhetNavnOgOrgNr.first)
	}

	private fun hentJuridiskEnhet(
		inngaarIJuridiskEnheter: List<InngaarIJuridiskEnheter>
	): Pair<String, String> {
		val juridiskEnhetOrgNummer = inngaarIJuridiskEnheter.first().organisasjonsnummer.toString()
		val juridiskEnhetNavn = inngaarIJuridiskEnheter.first().navn.navnelinje1
		return Pair(juridiskEnhetNavn, juridiskEnhetOrgNummer)
	}
	private fun hentJuridiskEnhetFraOrganisasjonsledd(
		bestaarAvOrganisasjonsledd: List<BestaarAvOrganisasjonsledd>
	): Pair<String, String> {
		var juridiskEnhetOrgNummer = ""
		var juridiskEnhetNavn = ""
		if(harOrganisasjonsleddOver(bestaarAvOrganisasjonsledd)){
				juridiskEnhetOrgNummer = bestaarAvOrganisasjonsledd.first().organisasjonsledd.organisasjonsleddOver.first().organisasjonsledd.inngaarIJuridiskEnheter.first().organisasjonsnummer.toString()
				juridiskEnhetNavn = bestaarAvOrganisasjonsledd.first().organisasjonsledd.organisasjonsleddOver.first().organisasjonsledd.inngaarIJuridiskEnheter.first().navn.navnelinje1
		}else{
			juridiskEnhetOrgNummer = bestaarAvOrganisasjonsledd.first().organisasjonsledd.inngaarIJuridiskEnheter.first().organisasjonsnummer.toString()
		  	juridiskEnhetNavn = bestaarAvOrganisasjonsledd.first().organisasjonsledd.inngaarIJuridiskEnheter.first().navn.navnelinje1
		}
		return Pair(juridiskEnhetNavn, juridiskEnhetOrgNummer)
	}

	private fun harOrganisasjonsleddOver(bestaarAvOrganisasjonsledd: List<BestaarAvOrganisasjonsledd>) =
		(bestaarAvOrganisasjonsledd.first().organisasjonsledd.inngaarIJuridiskEnheter.isEmpty()
				&& bestaarAvOrganisasjonsledd.first().organisasjonsledd.organisasjonsleddOver.isNotEmpty()
				&& bestaarAvOrganisasjonsledd.first().organisasjonsledd.organisasjonsleddOver.first().organisasjonsledd.inngaarIJuridiskEnheter.isNotEmpty())

	private fun harAdresseOgJuridiskOrgNr(
		inngaarIJuridiskEnheter: List<InngaarIJuridiskEnheter>?,
		bestaarAvOrganisasjonsledd: List<BestaarAvOrganisasjonsledd>?
	) = ((inngaarIJuridiskEnheter != null && inngaarIJuridiskEnheter.isNotEmpty()) || (bestaarAvOrganisasjonsledd != null && bestaarAvOrganisasjonsledd.isNotEmpty())) && organisasjonDetaljer.forretningsadresser.isNotEmpty()
}
