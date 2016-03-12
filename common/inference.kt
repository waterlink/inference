package com.tddfellow.inference

data class Relation(val name: String, val subject: String, val relative: String) {
    private fun matcherFor(value: String, getter: (Relation) -> String): (Relation) -> Boolean {
        return if (value == "?") {
            { getter(it) != "?" }
        } else {
            { getter(it) == value }
        }
    }

    val matchers = listOf(
            matcherFor(name) { it.name },
            matcherFor(subject) { it.subject },
            matcherFor(relative) { it.relative }
    )

    val extractors: List<(Relation) -> String> = listOf(
            { it -> it.name },
            { it -> it.subject },
            { it -> it.relative }
    )

    val freeExtractors = extractors.filter { it(this) == "?" }

    fun match(other: Relation): Boolean {
        return matchers.all { it(other) }
    }
}

val relations = mutableListOf<Relation>()

fun _clear() {
    relations.clear()
}

fun query(relation: Relation): List<Relation> {
    if (!relations.contains(relation)) {
        relations.add(relation)
    }

    return relations.filter { relation.match(it) }
}

fun query(relationString: String): List<Relation> {
    return query(relationFromString(relationString))
}

fun relationFromString(relationString: String): Relation {
    val found = relations
            .map { relationString.split(it.name) + listOf(it.name) }
            .find { it.count() == 3 }

    return if (found == null) {
        newRelationFromString(relationString)
    } else {
        Relation(found[2], found[0], found[1])
    }
}

fun newRelationFromString(relationString: String): Relation {
    val components = relationString.split("?")

    if (components.count() != 3) {
        throw RuntimeException("New relation should be in format '? <relation name> ?'")
    }

    return Relation(components[1], "?", "?")
}

fun join(
        left: List<Relation>, leftValue: (Relation) -> String,
        right: List<Relation>, rightValue: (Relation) -> String): List<String> {
    return left.flatMap { relation ->
        right.map { rightValue(it) }
                .filter { it == leftValue(relation) }
    }
}

fun join(
        left: String, leftValue: (Relation) -> String,
        right: String, rightValue: (Relation) -> String): List<String> {
    val leftRelation = relationFromString(left)
    val rightRelation = relationFromString(right)

    return join(
            query(leftRelation), leftValue,
            query(rightRelation), rightValue)
}

fun join(left: String, right: String): List<String> {
    val leftRelation = relationFromString(left)
    if (leftRelation.freeExtractors.count() == 0) {
        throw RuntimeException("join(left, right): left should have at least one '?' token")
    }

    val rightRelation = relationFromString(right)
    if (rightRelation.freeExtractors.count() == 0) {
        throw RuntimeException("join(left, right): right should have at least one '?' token")
    }

    return join(
            query(leftRelation), leftRelation.freeExtractors.first(),
            query(rightRelation), rightRelation.freeExtractors.first()
    )
}
