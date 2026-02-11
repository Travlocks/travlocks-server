package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.Region;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.entity.Tag;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateTag;
import org.umc.travlocksserver.domain.template.enums.TagType;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TagRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateTagRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.infra.ai.AiTagResponseDTO;
import org.umc.travlocksserver.infra.ai.HyperClovaSuggestionClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateTagService {

    private final TemplateRepository templateRepository;
    private final TemplateVlockRepository templateVlockRepository;
    private final CityRepository cityRepository;
    private final HyperClovaSuggestionClient hyperClovaSuggestionClient;
    private final TagRepository tagRepository;
    private final TemplateTagRepository templateTagRepository;

    public void generateTags(Long templateId, LocalDateTime now) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        List<Vlock> vlocks = templateVlockRepository.findDistinctVlocksByTemplateId(templateId);

        Region region = templateRepository.findRegionByTemplateId(templateId).get(0);
        String travelTheme = String.valueOf(template.getTravelTheme().getContent());
        String tripDays= template.getTripDays().getDescription();

        String travelTransit = switch(template.getTransportType()) {
            case WALK -> "도보";
            case TRANSIT -> "대중교통";
            case CAR -> "차";
        };

        List<String> fixedInfoTags = List.of(travelTheme, tripDays, travelTransit);
        List<String> cities = cityRepository.findNameByRegionId(region.getId());

        // AI 호출
        AiTagResponseDTO response = hyperClovaSuggestionClient.requestToAiForTag(String.valueOf(region), fixedInfoTags, cities, vlocks);

        template.increaseTagVersion();

        saveTemplateTags(template, List.of(region.getName()), TagType.REGION);
        saveTemplateTags(template, fixedInfoTags, TagType.FIXED_INFO);
        saveTemplateTags(template,response.cities(), TagType.CITY);
        saveTemplateTags(template, response.free(), TagType.FREE);
    }

    private void saveTemplateTags(Template template, List<String> tags, TagType tagType) {
        for (String t : tags) {
            System.out.println("이건 saveTemplateTags에서 호출 " + t);
            Tag tag = tagRepository.findByName(t)
                    .orElse(tagRepository.save(Tag.create(t)));

            TemplateTag templateTag = TemplateTag.create(
                    tag,
                    template,
                    tagType,
                    template.getTagVersion()
            );

            templateTagRepository.save(templateTag);
        }
    }
}
