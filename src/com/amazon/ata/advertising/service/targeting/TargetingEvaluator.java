package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.*;
import java.util.concurrent.*;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = false;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
        boolean allTruePredicates = true;
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<TargetingPredicateResult>> futureList = new ArrayList<>();
        for (TargetingPredicate predicate : targetingPredicates) {
          futureList.add(executor.submit(() -> predicate.evaluate(requestContext)));
        }
        for(Future<TargetingPredicateResult> f : futureList) {
           try { if(!f.get().equals(TargetingPredicateResult.TRUE)) {
               allTruePredicates = false;
           }

            } catch(InterruptedException | ExecutionException e) {
               e.printStackTrace();
           }
        }




        return allTruePredicates ? TargetingPredicateResult.TRUE :
                                   TargetingPredicateResult.FALSE;
//        return targetingGroup.getTargetingPredicates()
//                .stream()
//                .map(x -> x.evaluate(requestContext))
//                .anyMatch(x -> !x.isTrue()) ? TargetingPredicateResult.FALSE : TargetingPredicateResult.TRUE;

    }
}
