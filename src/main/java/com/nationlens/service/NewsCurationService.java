package com.nationlens.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rules-based awareness curation for ingested RSS items (no LLM — free + deterministic).
 *
 * <p>NationLens does not store everything a feed emits. Each item is scored for
 * civic-awareness value across four topics (elections, governance, legal, civic) with a
 * deliberate <b>middle/lower-class lens</b> — items about prices, jobs, ration, schemes,
 * healthcare, electricity, farmers etc. are boosted, because that is what ordinary citizens
 * most need to know. Sports/celebrity/entertainment noise is suppressed. Items that don't
 * clear {@code nationlens.curation.min-score} are dropped at ingestion.
 *
 * <p>An LLM re-rank pass can be layered on later for borderline items; the keyword rules
 * are the free first line.
 */
@Service
public class NewsCurationService {

    public record Verdict(boolean keep, int score, String topic, boolean classRelevant) {}

    @Value("${nationlens.curation.min-score:2}")
    private int minScore;

    // --- Topic keyword sets (awareness_topic value -> keywords) ----------------------------

    private static final Map<String, Set<String>> TOPICS = Map.of(
            "ELECTIONS", Set.of(
                    "election", "elections", "poll", "polls", "voter", "vote", "candidate",
                    "constituency", "nomination", "manifesto", "campaign", "ballot", "evm",
                    "eci", "election commission", "adr", "affidavit", "by-election", "byelection",
                    "lok sabha", "vidhan sabha", "assembly poll", "exit poll", "turnout"),
            "GOVERNANCE", Set.of(
                    "scheme", "yojana", "budget", "subsidy", "policy", "corruption", "scam",
                    "cbi", "enforcement directorate", " ed ", "rti", "tender", "governance",
                    "minister", "cabinet", "mla", " mp ", "parliament", "assembly", "bill",
                    "ordinance", "manifesto", "welfare", "pension", "reservation", "quota"),
            "LEGAL", Set.of(
                    "supreme court", "high court", "verdict", "judgment", "judgement", "pil",
                    "bench", "judge", "justice", "ruling", "petition", "bail", "fir", "tribunal",
                    "constitution", "fundamental right", "court orders", "hearing", "plea"),
            "CIVIC", Set.of(
                    "municipal", "corporation", "panchayat", "ward", "water supply", "sewage",
                    "garbage", "road", "pothole", "transport", "metro", "bus", "pollution",
                    "air quality", "hospital", "phc", "school", "civic body", "drainage",
                    "encroachment", "street light", "public service")
    );

    // --- Middle / lower-class "kitchen-table" lens — boosts everyday-life relevance ---------

    private static final Set<String> CLASS_RELEVANCE = Set.of(
            "price", "prices", "inflation", "petrol", "diesel", "lpg", "gas cylinder", "cylinder",
            "fuel", "ration", "pds", "mgnrega", "nrega", "employment", "job", "jobs", "unemployment",
            "farmer", "kisan", "msp", "crop", "loan waiver", "pension", "scholarship", "fees",
            "school fee", "reservation", "quota", "ews", "housing", "slum", "labour", "labor",
            "wage", "minimum wage", "daily wage", "healthcare", "ayushman", "medicine", "hospital",
            "electricity bill", "power cut", "water", "subsidy", "free", "welfare", "poverty",
            "below poverty line", "bpl", "migrant", "worker", "street vendor", "auto driver");

    // --- Noise to suppress when no civic signal is present ----------------------------------

    private static final List<String> NOISE = List.of(
            "cricket", "ipl", "bollywood", "box office", "trailer", "movie review", "celebrity",
            "horoscope", "astrology", "zodiac", "fashion", "recipe", "viral video", "web series",
            "ott release", "football transfer", "fifa", "grammy", "oscar buzz", "gossip",
            "engagement rumour", "wedding photos", "fashion week", "tiktok");

    public Verdict evaluate(String title, String description, String category) {
        String text = (" " + nz(title) + " " + nz(description) + " ").toLowerCase();

        int score = 0;
        String bestTopic = null;
        int bestTopicHits = 0;

        for (var entry : TOPICS.entrySet()) {
            int hits = countHits(text, entry.getValue());
            if (hits > 0) {
                score += hits;
                if (hits > bestTopicHits) {
                    bestTopicHits = hits;
                    bestTopic = entry.getKey();
                }
            }
        }

        // Category from the feed registry is a strong prior — ADR/LEGAL/ELECTIONS/CIVIC are
        // already civic by definition, so give them a baseline boost (and a topic fallback).
        String cat = nz(category).toUpperCase();
        if (cat.equals("ADR") || cat.equals("ELECTIONS")) { score += 2; if (bestTopic == null) bestTopic = "ELECTIONS"; }
        else if (cat.equals("LEGAL")) { score += 2; if (bestTopic == null) bestTopic = "LEGAL"; }
        else if (cat.equals("CIVIC") || cat.equals("CITY_NEWS")) { score += 1; if (bestTopic == null) bestTopic = "CIVIC"; }

        // Middle/lower-class relevance: strong boost — this is the editorial priority.
        int classHits = countHits(text, CLASS_RELEVANCE);
        boolean classRelevant = classHits > 0;
        if (classRelevant) {
            score += 2 + classHits;
            // Everyday-life stories (prices, jobs, ration…) are awareness-worthy even when they
            // hit no explicit topic keyword. File them under GOVERNANCE so they're kept & ranked.
            if (bestTopic == null) bestTopic = "GOVERNANCE";
        }

        // Suppress pure noise that carries no civic/class signal at all.
        if (score == 0 && containsAny(text, NOISE)) {
            return new Verdict(false, 0, null, false);
        }

        boolean keep = score >= minScore && bestTopic != null;
        return new Verdict(keep, score, bestTopic, classRelevant);
    }

    private static int countHits(String text, Set<String> keywords) {
        int hits = 0;
        for (String kw : keywords) {
            if (text.contains(kw)) hits++;
        }
        return hits;
    }

    private static boolean containsAny(String text, List<String> needles) {
        for (String n : needles) if (text.contains(n)) return true;
        return false;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
