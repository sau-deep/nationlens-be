package com.nationlens.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/** Plain unit test (no Spring context) for the rules-based awareness curation. */
class NewsCurationServiceTest {

    private NewsCurationService svc() {
        NewsCurationService s = new NewsCurationService();
        ReflectionTestUtils.setField(s, "minScore", 2);
        return s;
    }

    @Test
    void keepsElectionNews() {
        var v = svc().evaluate("Election Commission announces poll dates for assembly", null, "ELECTIONS");
        assertTrue(v.keep());
        assertEquals("ELECTIONS", v.topic());
    }

    @Test
    void keepsKitchenTableStoryEvenWithoutTopicKeyword() {
        // Pure middle/lower-class concern, no explicit topic keyword.
        var v = svc().evaluate("Petrol and diesel prices hiked again nationwide", null, "NATIONAL_NEWS");
        assertTrue(v.keep(), "everyday price story must be kept");
        assertTrue(v.classRelevant());
        assertEquals("GOVERNANCE", v.topic(), "kitchen-table item with no topic keyword files under GOVERNANCE");
    }

    @Test
    void dropsSportsNoise() {
        var v = svc().evaluate("IPL: Kohli scores century as RCB beat CSK in thriller", null, "NATIONAL_NEWS");
        assertFalse(v.keep());
    }

    @Test
    void dropsCelebrityNoise() {
        var v = svc().evaluate("Bollywood star spotted at airport, fans go viral", null, "NATIONAL_NEWS");
        assertFalse(v.keep());
    }

    @Test
    void keepsCourtVerdict() {
        var v = svc().evaluate("Supreme Court verdict on electoral bonds petition", null, "LEGAL");
        assertTrue(v.keep());
        assertEquals("LEGAL", v.topic());
    }

    @Test
    void boostsRationScheme() {
        var v = svc().evaluate("Free ration scheme extended, MGNREGA wages raised for workers", null, "CIVIC");
        assertTrue(v.keep());
        assertTrue(v.classRelevant());
    }
}
