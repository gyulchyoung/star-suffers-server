package com.server.tourApiProject.observation;

import com.server.tourApiProject.observation.observeHashTag.ObserveHashTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationSpecificationTest {

    @Mock
    private Root<Observation> root;

    @Mock
    private CriteriaQuery<Object> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate observationNamePredicate;

    @Mock
    private Predicate outlinePredicate;

    @Mock
    private Predicate combinedPredicate;

    @Mock
    private Predicate hashTagEqualPredicate;

    @Mock
    private Predicate hashTagInPredicate;

    @Mock
    private Predicate hashTagAndPredicate;

    @Mock
    private Predicate existsPredicate;

    @Mock
    private Predicate areaPredicate;

    @Mock
    private Predicate orPredicate;

    @Mock
    private Predicate finalPredicate;

    @Mock
    private Path<String> observationNamePath;

    @Mock
    private Path<String> outlinePath;

    @Mock
    private Path<Object> observationIdPath;

    @Mock
    private Path<Object> areaCodePath;

    @Mock
    private Path<Object> subObservationIdPath;

    @Mock
    private Path<Object> subHashTagIdPath;

    @Mock
    private Root<ObserveHashTag> observeHashTagRoot;

    @Mock
    private Subquery<ObserveHashTag> subquery;

    @Test
    void likeSearchKey_combinesObservationNameAndOutlineWithAndPredicate() {
        when(root.<String>get("observationName")).thenReturn(observationNamePath);
        when(root.<String>get("outline")).thenReturn(outlinePath);
        when(criteriaBuilder.like(observationNamePath, "%night%")).thenReturn(observationNamePredicate);
        when(criteriaBuilder.like(outlinePath, "%night%")).thenReturn(outlinePredicate);
        when(criteriaBuilder.and(observationNamePredicate, outlinePredicate)).thenReturn(combinedPredicate);

        Specification<Observation> specification = ObservationSpecification.likeSearchKey("night");

        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertSame(combinedPredicate, result);
    }

    @Test
    void existHashtagId_buildsExistsSubqueryForRequestedHashtags() {
        List<Long> hashtagIds = List.of(1L, 2L);
        when(query.subquery(ObserveHashTag.class)).thenReturn(subquery);
        when(subquery.from(ObserveHashTag.class)).thenReturn(observeHashTagRoot);
        when(root.get("observationId")).thenReturn(observationIdPath);
        when(observeHashTagRoot.get("observationId")).thenReturn(subObservationIdPath);
        when(observeHashTagRoot.get("hashTagId")).thenReturn(subHashTagIdPath);
        when(criteriaBuilder.equal(subObservationIdPath, observationIdPath)).thenReturn(hashTagEqualPredicate);
        when(subHashTagIdPath.in(hashtagIds)).thenReturn(hashTagInPredicate);
        when(criteriaBuilder.and(hashTagInPredicate)).thenReturn(hashTagAndPredicate);
        when(subquery.select(observeHashTagRoot)).thenReturn(subquery);
        when(subquery.where(hashTagEqualPredicate, hashTagAndPredicate)).thenReturn(subquery);
        when(criteriaBuilder.exists(subquery)).thenReturn(existsPredicate);

        Specification<Observation> specification = ObservationSpecification.existHashtagId(hashtagIds);

        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertSame(existsPredicate, result);
    }

    @Test
    void inAreaCodes_wrapsInPredicateWithAndPredicate() {
        List<Long> areaCodes = List.of(10L, 20L);
        when(root.get("areaCode")).thenReturn(areaCodePath);
        when(areaCodePath.in(areaCodes)).thenReturn(areaPredicate);
        when(criteriaBuilder.and(areaPredicate)).thenReturn(combinedPredicate);

        Specification<Observation> specification = ObservationSpecification.inAreaCodes(areaCodes);

        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertSame(combinedPredicate, result);
    }

    @Test
    void likeSearchKeyAndInFilter_appliesAllAvailableConditions() {
        List<Long> hashtagIds = List.of(5L);
        List<Long> areaCodes = List.of(39L);
        when(root.<String>get("observationName")).thenReturn(observationNamePath);
        when(root.<String>get("outline")).thenReturn(outlinePath);
        when(root.get("observationId")).thenReturn(observationIdPath);
        when(root.get("areaCode")).thenReturn(areaCodePath);
        when(criteriaBuilder.like(observationNamePath, "%sky%")).thenReturn(observationNamePredicate);
        when(criteriaBuilder.like(outlinePath, "%sky%")).thenReturn(outlinePredicate);
        when(criteriaBuilder.or(observationNamePredicate, outlinePredicate)).thenReturn(orPredicate);
        when(query.subquery(ObserveHashTag.class)).thenReturn(subquery);
        when(subquery.from(ObserveHashTag.class)).thenReturn(observeHashTagRoot);
        when(observeHashTagRoot.get("observationId")).thenReturn(subObservationIdPath);
        when(observeHashTagRoot.get("hashTagId")).thenReturn(subHashTagIdPath);
        when(criteriaBuilder.equal(subObservationIdPath, observationIdPath)).thenReturn(hashTagEqualPredicate);
        when(subHashTagIdPath.in(hashtagIds)).thenReturn(hashTagInPredicate);
        when(criteriaBuilder.and(hashTagInPredicate)).thenReturn(hashTagAndPredicate);
        when(subquery.select(observeHashTagRoot)).thenReturn(subquery);
        when(subquery.where(hashTagEqualPredicate, hashTagAndPredicate)).thenReturn(subquery);
        when(criteriaBuilder.exists(subquery)).thenReturn(existsPredicate);
        when(areaCodePath.in(areaCodes)).thenReturn(areaPredicate);
        when(criteriaBuilder.and(areaPredicate)).thenReturn(areaPredicate);
        when(criteriaBuilder.and(orPredicate, existsPredicate, areaPredicate)).thenReturn(finalPredicate);

        Specification<Observation> specification =
                ObservationSpecification.likeSearchKeyAndInFilter("sky", hashtagIds, areaCodes);

        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).and(orPredicate, existsPredicate, areaPredicate);
    }

    @Test
    void likeSearchKeyAndInFilter_skipsOptionalConditionsWhenInputsAreEmpty() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(finalPredicate);

        Specification<Observation> specification =
                ObservationSpecification.likeSearchKeyAndInFilter("", Collections.emptyList(), Collections.emptyList());

        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertSame(finalPredicate, result);
        verify(root, never()).get(eq("observationName"));
        verify(query, never()).subquery(ObserveHashTag.class);
    }
}
