package com.alex.rwp.controller;

import com.alex.rwp.proto.AlphaProto.Alpha;
import com.alex.rwp.proto.BetaProto.Beta;
import com.alex.rwp.proto.EntityProto.Entity;
import com.alex.rwp.proto.RefProto.Ref;
import com.google.protobuf.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/kafka")
@Tag(name = "Kafka", description = "Generate and publish protobuf messages to Kafka")
public class KafkaProducerController {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String[] WORDS = {
        "alpha", "beta", "gamma", "delta", "echo", "foxtrot", "hotel",
        "india", "juliet", "kilo", "lima", "mike", "november", "oscar"
    };

    private final KafkaTemplate<String, byte[]> kafka;
    private final AtomicLong idGen = new AtomicLong(System.currentTimeMillis());

    private final String topicEntities;
    private final String topicAlpha;
    private final String topicBeta;
    private final String topicRefs;

    @SuppressWarnings("unchecked")
    public KafkaProducerController(KafkaTemplate<String, ?> kafka,
                                   @Value("${app.kafka.topics.entities}") String topicEntities,
                                   @Value("${app.kafka.topics.alpha}")    String topicAlpha,
                                   @Value("${app.kafka.topics.beta}")     String topicBeta,
                                   @Value("${app.kafka.topics.refs}")     String topicRefs) {
        this.kafka         = (KafkaTemplate<String, byte[]>) kafka;
        this.topicEntities = topicEntities;
        this.topicAlpha    = topicAlpha;
        this.topicBeta     = topicBeta;
        this.topicRefs     = topicRefs;
    }

    @PostMapping("/generate/entities")
    @Operation(summary = "Generate events and publish to the entities topic")
    public Map<String, Object> generateEntities(@RequestParam(defaultValue = "100000") int count) {
        Instant now = Instant.now();
        long start = System.currentTimeMillis();

        long sent = stream(
            Stream.iterate(0, i -> i + 1).limit(count).map(i ->
                Entity.newBuilder()
                    .setId(idGen.incrementAndGet())
                    .setType("GENERATED")
                    .setPayload("event payload " + i)
                    .setTs(now.toString())
                    .build()
            ),
            msg -> String.valueOf(msg.getId()),
            topicEntities
        );

        return Map.of("topic", topicEntities, "count", sent, "elapsed_ms", System.currentTimeMillis() - start);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate alpha, beta, refs and publish each to their topic")
    public Map<String, Object> generate(@RequestParam(defaultValue = "100000") int count) {
        var r   = ThreadLocalRandom.current();
        Instant now = Instant.now();

        List<Long> alphaIds = new ArrayList<>(count);
        List<Long> betaIds  = new ArrayList<>(count);

        long t0 = System.currentTimeMillis();
        stream(
            Stream.iterate(0, i -> i + 1).limit(count).map(i -> {
                long id = idGen.incrementAndGet();
                alphaIds.add(id);
                var b = Alpha.newBuilder()
                    .setId(id)
                    .setAlphaName(randomWord(r) + "_" + i)
                    .setAlphaCode(randomChar(r))
                    .setAlphaCount(r.nextInt(1000))
                    .setAlphaValue(r.nextLong(1_000_000L))
                    .setAlphaScore(r.nextInt(100))
                    .setAlphaAmount(r.nextLong(1_000_000_000L))
                    .setAlphaDescription("Description for alpha " + i)
                    .setAlphaStatus(randomChar(r))
                    .setAlphaCreatedAt(now.minus(r.nextInt(30), ChronoUnit.DAYS).toString());
                if (r.nextBoolean()) b.setAlphaUpdatedAt(now.toString());
                return b.build();
            }),
            msg -> String.valueOf(msg.getId()),
            topicAlpha
        );
        long alphaMs = System.currentTimeMillis() - t0;

        long t1 = System.currentTimeMillis();
        stream(
            Stream.iterate(0, i -> i + 1).limit(count).map(i -> {
                long id = idGen.incrementAndGet();
                betaIds.add(id);
                var b = Beta.newBuilder()
                    .setId(id)
                    .setBetaTitle(randomWord(r) + "_" + i)
                    .setBetaRefCode(randomChar(r))
                    .setBetaQuantity(r.nextInt(500))
                    .setBetaTotal(r.nextLong(999_999L))
                    .setBetaPriority(r.nextInt(10))
                    .setBetaSequence(r.nextInt(10_000))
                    .setBetaWeight(r.nextLong(10_000_000L))
                    .setBetaChecksum(r.nextLong(Long.MAX_VALUE))
                    .setBetaNotes("Notes for beta " + i)
                    .setBetaLabel("label_" + randomWord(r))
                    .setBetaCategory(randomChar(r))
                    .setBetaTag(randomChar(r))
                    .setBetaCreatedAt(now.minus(r.nextInt(30), ChronoUnit.DAYS).toString());
                if (r.nextBoolean()) b.setBetaUpdatedAt(now.toString());
                if (r.nextBoolean()) b.setBetaExpiresAt(now.plus(r.nextInt(90), ChronoUnit.DAYS).toString());
                return b.build();
            }),
            msg -> String.valueOf(msg.getId()),
            topicBeta
        );
        long betaMs = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        var seen = new HashSet<String>();
        long refCount = stream(
            alphaIds.stream().flatMap(alphaId -> {
                int linkCount = r.nextInt(1, Math.min(4, betaIds.size() + 1));
                return r.ints(0, betaIds.size()).distinct().limit(linkCount)
                    .mapToObj(betaIds::get)
                    .filter(betaId -> seen.add(alphaId + ":" + betaId))
                    .map(betaId -> Ref.newBuilder()
                        .setAlphaId(alphaId)
                        .setBetaId(betaId)
                        .setCreatedAt(now.toString())
                        .build());
            }),
            msg -> msg.getAlphaId() + ":" + msg.getBetaId(),
            topicRefs
        );
        long refsMs = System.currentTimeMillis() - t2;

        return Map.of(
            "alpha",       count,    "alpha_ms",  alphaMs,
            "beta",        count,    "beta_ms",   betaMs,
            "refs",        refCount, "refs_ms",   refsMs,
            "total_ms",    alphaMs + betaMs + refsMs
        );
    }

    @PostMapping("/generate/continuous")
    @Operation(summary = "Continuously generate at a fixed rate for a given duration")
    public Map<String, Object> continuousGeneration(
            @RequestParam(defaultValue = "10000") int rate,
            @RequestParam(defaultValue = "300")   int durationSeconds,
            @RequestParam(defaultValue = "1000")  int batchSize) throws InterruptedException {

        long deadline = System.currentTimeMillis() + durationSeconds * 1000L;
        long totalAlpha = 0, totalBeta = 0, totalRefs = 0, totalMs = 0;
        int  iterations = 0;

        while (System.currentTimeMillis() < deadline) {
            long windowStart = System.currentTimeMillis();

            for (int i = 0; i < rate && System.currentTimeMillis() < deadline; i++) {
                Map<String, Object> result = generate(batchSize);
                totalAlpha += (int)  result.get("alpha");
                totalBeta  += (int)  result.get("beta");
                totalRefs  += (long) result.get("refs");
                totalMs    += (long) result.get("total_ms");
                iterations++;
            }

            long remaining = 1000L - (System.currentTimeMillis() - windowStart);
            if (remaining > 0) Thread.sleep(remaining);
        }

        return Map.of(
            "iterations",       iterations,
            "alpha",            totalAlpha,
            "beta",             totalBeta,
            "refs",             totalRefs,
            "generate_ms_total", totalMs,
            "elapsed_ms",       (long) durationSeconds * 1000
        );
    }

    private <T extends Message> long stream(Stream<T> messages, Function<T, String> keyFn, String topic) {
        return messages.peek(msg -> kafka.send(topic, keyFn.apply(msg), msg.toByteArray())).count();
    }

    private String randomChar(ThreadLocalRandom r) {
        return String.valueOf(CHARS.charAt(r.nextInt(CHARS.length())));
    }

    private String randomWord(ThreadLocalRandom r) {
        return WORDS[r.nextInt(WORDS.length)];
    }
}
