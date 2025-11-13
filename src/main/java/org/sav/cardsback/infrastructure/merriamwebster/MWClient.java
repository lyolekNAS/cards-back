package org.sav.cardsback.infrastructure.merriamwebster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.model.mw.MWEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MWClient {

	private final RestTemplate restTemplate;

	@Value("${app-props.url.merriam-webster}")
	private String baseURL;
	@Value("${merriam_webster_key}")
	private String apiKey;

	public List<MWEntry> fetchWord(String word) {
		log.debug("fetchWord: {}", word);

		String url = UriComponentsBuilder.fromHttpUrl(baseURL)
				.pathSegment(word)
				.queryParam("key", apiKey)
				.toUriString();

		try {
			String json = restTemplate.getForObject(url, String.class);

			JsonNode root = new ObjectMapper().readTree(json);

			if (!root.isArray() || root.isEmpty()) {
				log.warn("Word {} is FAKE (empty array)", word);
				return List.of();
			}

			JsonNode first = root.get(0);

			if (first.isTextual()) {
				log.warn("Word {} is FAKE (text suggestions)", word);
				return List.of();
			}

			MWEntry[] mwResp = new ObjectMapper().readValue(json, MWEntry[].class);
			return List.of(mwResp);

		} catch (Exception e) {
			log.error("Failed to fetch word {}: {}", word, e.getMessage());
			return List.of();
		}


//		String mwResp = restTemplate.postForObject(url, word, String.class);
//		log.info("mwResp:{}", mwResp);
//		return new ArrayList<>();

//		String json = "[{\"meta\":{\"id\":\"peace:1\",\"uuid\":\"d646f191-e8ba-47f9-ac54-280da7fb65a6\",\"sort\":\"171350000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"a breach of the peace\",\"at peace\",\"disturbing the peace\",\"in peace\",\"keeping the peace\",\"make her peace\",\"peace\",\"peace and quiet\",\"peace of mind\",\"peaces\",\"rest in peace\"],\"offensive\":false},\"hom\":1,\"hwi\":{\"hw\":\"peace\",\"prs\":[{\"mw\":\"ˈpēs\",\"sound\":{\"audio\":\"peace001\",\"ref\":\"c\",\"stat\":\"1\"}}]},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"bs\",{\"sense\":{\"sn\":\"1\",\"dt\":[[\"text\",\"{bc}a state of tranquility or quiet: such as\"]]}}],[\"sense\",{\"sn\":\"a\",\"dt\":[[\"text\",\"{bc}freedom from civil disturbance \"],[\"vis\",[{\"t\":\"{wi}Peace{\\/wi} and order were finally restored in the town.\"}]]]}],[\"sense\",{\"sn\":\"b\",\"dt\":[[\"text\",\"{bc}a state of security or order within a community provided for by law or custom \"],[\"vis\",[{\"t\":\"was arrested for {phrase}a breach of the peace{\\/phrase} = was arrested for {phrase}disturbing the peace{\\/phrase} {gloss}=for behaving in a loud or violent way in a public place{\\/gloss}\"}]]]}],[\"sense\",{\"sn\":\"c\",\"dt\":[[\"text\",\"{bc}freedom from being disturbed or bothered by people, noise, etc. \"],[\"vis\",[{\"t\":\"went outside for a few moments of {wi}peace{\\/wi}\"},{\"t\":\"Why won't they leave him {phrase}in peace{\\/phrase}?\"},{\"t\":\"enjoyed the {phrase}peace and quiet{\\/phrase} of the library\"}]]]}]],[[\"sense\",{\"sn\":\"2\",\"dt\":[[\"text\",\"{bc}freedom from disquieting or oppressive thoughts or emotions \"],[\"vis\",[{\"t\":\"Spending more time in nature has helped me achieve inner {wi}peace{\\/wi}.\"},{\"t\":\"I have been in perfect {wi}peace{\\/wi} and contentment …\",\"aq\":{\"auth\":\"J. H. Newman\"}},{\"t\":\"Insurance can provide you with {phrase}peace of mind{\\/phrase}.\"},{\"t\":\"May our dearly departed friend {phrase}rest in peace{\\/phrase}.\"}]]]}]],[[\"sense\",{\"sn\":\"3\",\"dt\":[[\"text\",\"{bc}harmony in personal relations \"],[\"vis\",[{\"t\":\"There will be no {wi}peace{\\/wi} in this house until one of them apologizes.\"},{\"t\":\"She wanted to {phrase}make her peace{\\/phrase} {gloss}=end an argument or disagreement{\\/gloss} with her father before he died.\"},{\"t\":\"… many eschewed political talk in favor of {phrase}keeping the peace{\\/phrase} between friends and neighbors.\",\"aq\":{\"auth\":\"Jordyn Hermani\"}}]]]}]],[[\"sense\",{\"sn\":\"4 a\",\"dt\":[[\"text\",\"{bc}a state or period of mutual concord between governments \"],[\"vis\",[{\"t\":\"There was a {wi}peace{\\/wi} of 50 years before war broke out again.\"}]]]}],[\"sense\",{\"sn\":\"b\",\"dt\":[[\"text\",\"{bc}a pact or agreement to end hostilities between those who have been at war or in a state of enmity \"],[\"vis\",[{\"t\":\"… few believe a negotiated {wi}peace{\\/wi} is imminent.\",\"aq\":{\"auth\":\"Jason Horowitz\"}}]],[\"uns\",[[[\"text\",\"often used before another noun \"],[\"vis\",[{\"t\":\"a {wi}peace{\\/wi} agreement\\/accord\\/treaty\"},{\"t\":\"a {wi}peace{\\/wi} initiative\\/conference\"},{\"t\":\"The two sides have held no substantive {wi}peace{\\/wi} talks in more than a decade.\",\"aq\":{\"auth\":\"Joseph Krauss\"}}]]]]]]}]],[[\"sense\",{\"sn\":\"5\",\"dt\":[[\"uns\",[[[\"text\",\"used interjectionally to ask for silence or calm or as a greeting or farewell\"]]]]]}]]]}],\"dros\":[{\"drp\":\"at peace\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}in a state of harmony or tranquility \"],[\"vis\",[{\"t\":\"The problem was settled and his mind was {it}at peace{\\/it}.\"},{\"t\":\"She's {it}at peace{\\/it} with their decision.\"},{\"t\":\"The sisters are {it}at peace{\\/it} with each other.\"}]]]}]]]}]}],\"et\":[[\"text\",\"Middle English {it}pees{\\/it}, from Anglo-French {it}pes, pees{\\/it}, from Latin {it}pac-, pax{\\/it}; akin to Latin {it}pacisci{\\/it} to agree {ma}{mat|pact|}{\\/ma}\"]],\"date\":\"12th century{ds||1||}\",\"shortdef\":[\"a state of tranquility or quiet: such as\",\"freedom from civil disturbance\",\"a state of security or order within a community provided for by law or custom\"]},{\"meta\":{\"id\":\"peace:2\",\"uuid\":\"9c0545ba-a518-44bd-a6ba-16d080860ed6\",\"sort\":\"171351000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace\",\"peaced\",\"peaces\",\"peacing\"],\"offensive\":false},\"hom\":2,\"hwi\":{\"hw\":\"peace\"},\"fl\":\"verb\",\"ins\":[{\"if\":\"peaced\"},{\"if\":\"peac*ing\"},{\"if\":\"peac*es\"}],\"def\":[{\"vd\":\"intransitive verb\",\"sls\":[\"obsolete\"],\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}to be, become, or keep silent or quiet\"]]}]]]}],\"et\":[[\"text\",\"{dx_ety}see {dxt|peace:1||}{\\/dx_ety}\"]],\"date\":\"14th century\",\"shortdef\":[\"to be, become, or keep silent or quiet\"]},{\"meta\":{\"id\":\"Peace:g\",\"uuid\":\"d90a7558-9053-4f26-8e26-a300a2b7c45e\",\"sort\":\"341645000\",\"src\":\"collegiate\",\"section\":\"geog\",\"stems\":[\"Peace\"],\"offensive\":false},\"hwi\":{\"hw\":\"Peace\",\"prs\":[{\"mw\":\"ˈpēs\",\"sound\":{\"audio\":\"ggpeac01\",\"ref\":\"c\",\"stat\":\"1\"}}]},\"fl\":\"geographical name\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"river 1195 miles (1923 kilometers) long in western Canada flowing east and northeast in northern British Columbia and northern Alberta into the Slave River {dx}see {dxt|finlay||}{\\/dx}\"]]}]]]}],\"shortdef\":[\"river 1195 miles (1923 kilometers) long in western Canada flowing east and northeast in northern British Columbia and northern Alberta into the Slave River\"]},{\"meta\":{\"id\":\"peace corps\",\"uuid\":\"2481d668-e7ca-442b-a4f3-63271186b444\",\"sort\":\"171353000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace corps\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace corps\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}a body of trained personnel sent as volunteers especially to assist underdeveloped nations\"]]}]]]}],\"date\":\"1960\",\"shortdef\":[\"a body of trained personnel sent as volunteers especially to assist underdeveloped nations\"]},{\"meta\":{\"id\":\"peace dividend\",\"uuid\":\"1d06a2f6-f6e5-45cb-8a02-c1ed5deef086\",\"sort\":\"171354000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace dividend\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace dividend\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}a portion of funds made available for nondefense spending by a reduction in the defense budget (as after a war)\"]]}]]]}],\"date\":\"1968\",\"shortdef\":[\"a portion of funds made available for nondefense spending by a reduction in the defense budget (as after a war)\"]},{\"meta\":{\"id\":\"peace offering\",\"uuid\":\"db6f503a-525d-41a2-a7a4-db0cfe276c91\",\"sort\":\"171359000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace offering\",\"peace offerings\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace offering\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}a gift or service for the purpose of procuring peace or reconciliation\"]]}]]]}],\"date\":\"circa 1607\",\"shortdef\":[\"a gift or service for the purpose of procuring peace or reconciliation\"]},{\"meta\":{\"id\":\"peace officer\",\"uuid\":\"ae2da4e2-7d26-4a8f-958b-1d54b34a82da\",\"sort\":\"171360000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace officer\",\"peace officers\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace officer\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}a civil officer (such as a police officer) whose duty it is to preserve the public peace\"]]}]]]}],\"date\":\"1649\",\"shortdef\":[\"a civil officer (such as a police officer) whose duty it is to preserve the public peace\"]},{\"meta\":{\"id\":\"peace pipe\",\"uuid\":\"cd1002ec-a0a4-487f-9e5f-d34540801c8f\",\"sort\":\"171361000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace pipe\",\"peace pipes\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace pipe\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}{sx|calumet||}\"]]}]]]}],\"date\":\"1760\",\"shortdef\":[\"calumet\"]},{\"meta\":{\"id\":\"peace sign\",\"uuid\":\"beb70610-5bed-4bb8-bc33-e98f5373b179\",\"sort\":\"171362000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace sign\",\"peace signs\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace sign\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"sn\":\"1\",\"dt\":[[\"text\",\"{bc}a sign made by holding the palm outward and forming a V with the index and middle fingers and used to indicate the desire for peace\"]]}]],[[\"sense\",{\"sn\":\"2\",\"dt\":[[\"text\",\"{bc}{sx|peace symbol||}\"]]}]]]}],\"date\":\"1968{ds||1||}\",\"shortdef\":[\"a sign made by holding the palm outward and forming a V with the index and middle fingers and used to indicate the desire for peace\",\"peace symbol\"]},{\"meta\":{\"id\":\"peace symbol\",\"uuid\":\"9492d6ce-9dc3-4275-b34a-5865438970b7\",\"sort\":\"171363000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"peace symbol\",\"peace symbols\"],\"offensive\":false},\"hwi\":{\"hw\":\"peace symbol\"},\"fl\":\"noun\",\"def\":[{\"sseq\":[[[\"sense\",{\"dt\":[[\"text\",\"{bc}the symbol ☮ used to signify peace\"]]}]]]}],\"date\":\"1968\",\"shortdef\":[\"the symbol ☮ used to signify peace\"]}]";
//		String json = "[{\"meta\":{\"id\":\"deduce\",\"uuid\":\"c5ec3eb7-780a-4801-813d-c4aee917cdff\",\"sort\":\"050789000\",\"src\":\"collegiate\",\"section\":\"alpha\",\"stems\":[\"deduce\",\"deduced\",\"deduces\",\"deducible\",\"deducing\"],\"offensive\":false},\"hwi\":{\"hw\":\"de*duce\",\"prs\":[{\"mw\":\"di-ˈdüs\",\"sound\":{\"audio\":\"deduce01\",\"ref\":\"c\",\"stat\":\"1\"}},{\"mw\":\"dē-\",\"pun\":\";\"},{\"l\":\"chiefly British\",\"mw\":\"-ˈdyüs\"}]},\"fl\":\"verb\",\"ins\":[{\"if\":\"de*duced\"},{\"if\":\"de*duc*ing\"}],\"def\":[{\"vd\":\"transitive verb\",\"sseq\":[[[\"sense\",{\"sn\":\"1\",\"dt\":[[\"text\",\"{bc}to determine by reasoning or {d_link|deduction|deduction} \"],[\"vis\",[{\"t\":\"{wi}deduce{\\/wi} the age of ancient artifacts\"},{\"t\":\"She {wi}deduced{\\/wi}, from the fur stuck to his clothes, that he owned a cat.\"}]]],\"sdsense\":{\"sd\":\"specifically\",\"sls\":[\"philosophy\"],\"dt\":[[\"text\",\"{bc}to infer {dx_def}see {dxt|infer||1}{\\/dx_def} from a general principle\"]]}}]],[[\"sense\",{\"sn\":\"2\",\"dt\":[[\"text\",\"{bc}to trace the course of \"],[\"vis\",[{\"t\":\"{wi}deduce{\\/wi} their lineage\"}]]]}]]]}],\"uros\":[{\"ure\":\"de*duc*ible\",\"prs\":[{\"mw\":\"di-ˈd(y)ü-sə-bəl\",\"sound\":{\"audio\":\"deduce02\",\"ref\":\"c\",\"stat\":\"1\"}},{\"mw\":\"dē-\"}],\"fl\":\"adjective\"}],\"syns\":[{\"pl\":\"synonyms\",\"pt\":[[\"text\",\"{sc}infer{\\/sc} {sc}deduce{\\/sc} {sc}conclude{\\/sc} {sc}judge{\\/sc} {sc}gather{\\/sc} mean to arrive at a mental conclusion. {sc}infer{\\/sc} implies arriving at a conclusion by reasoning from evidence; if the evidence is slight, the term comes close to {it}surmise{\\/it}. \"],[\"vis\",[{\"t\":\"from that remark, I {it}inferred{\\/it} that they knew each other\"}]],[\"text\",\" {sc}deduce{\\/sc} often adds to {sc}infer{\\/sc} the special implication of drawing a particular inference from a generalization. \"],[\"vis\",[{\"t\":\"denied we could {it}deduce{\\/it} anything important from human mortality\"}]],[\"text\",\" {sc}conclude{\\/sc} implies arriving at a necessary inference at the end of a chain of reasoning. \"],[\"vis\",[{\"t\":\"{it}concluded{\\/it} that only the accused could be guilty\"}]],[\"text\",\" {sc}judge{\\/sc} stresses a weighing of the evidence on which a conclusion is based. \"],[\"vis\",[{\"t\":\"{it}judge{\\/it} people by their actions\"}]],[\"text\",\" {sc}gather{\\/sc} suggests an intuitive forming of a conclusion from implications. \"],[\"vis\",[{\"t\":\"{it}gathered{\\/it} their desire to be alone without a word\"}]]]}],\"et\":[[\"text\",\"Middle English, from Latin {it}deducere{\\/it}, literally, to lead away, from {it}de-{\\/it} + {it}ducere{\\/it} to lead {ma}{mat|tow:1|}{\\/ma}\"]],\"date\":\"15th century{ds||1||}\",\"shortdef\":[\"to determine by reasoning or deduction; specifically, philosophy : to infer from a general principle\",\"to trace the course of\"]}]";
//		ObjectMapper mapper = new ObjectMapper();
//
//		List<MWEntry> entries = mapper.readValue(json, new TypeReference<>() {
//		});
//		return entries;
	}
}

