package com.tddfellow.inference.editor

import com.tddfellow.inference.Relation
import com.tddfellow.inference.join
import com.tddfellow.inference.relationFromString
import org.w3c.dom.DragEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.dom.appendText
import kotlin.dom.on
import kotlin.dom.onClick

val queryRegistry = mutableListOf<Query>()
val joinRegistry = mutableListOf<Join>()

class Query(val query: String, val id: String, val queries: Element) {
    val relation = relationFromString(query)

    val queryContainer = createContainer("${id}__query", queries)

    val relationSubject = createContainer("${id}__query__subject", queryContainer)
            .withText(relation.subject)
            .withClass("queries__query__matcher")
            .joinable("subject")

    val relationName = createContainer("${id}__query__name", queryContainer)
            .withText(relation.name)
            .withClass("queries__query__relation-name")

    val relationRelative = createContainer("${id}__query__relative", queryContainer)
            .withText(relation.relative)
            .withClass("queries__query__matcher")
            .joinable("relative")

    val resultContainer = createContainer("${id}__result", queries)

    var cachedResult = listOf<Relation>()

    val kinds = mapOf<String, (Relation) -> String>(
            Pair("subject", { it: Relation -> it.subject }),
            Pair("relative", { it -> it.relative })
    )

    fun execute() {
        val newResult = com.tddfellow.inference.query(query)

        if (newResult == cachedResult) { return }
        cachedResult = newResult

        resultContainer.textContent = ""
        newResult.forEach { relation ->
            resultContainer.appendText(relation.toString())
            resultContainer.appendChild(document.createElement("br"))
        }
    }

    private fun Element.joinable(kind: String): Element {
        this.setAttribute("draggable", "true")

        this.on("dragstart", true) {
            val event = it as DragEvent?
            val target = event?.target as Element?

            if (target != null) {
                event?.dataTransfer?.setData("text", JSON.stringify(listOf(kind, query, id)))
            }
        }

        this.on("dragover", true) { it.preventDefault() }

        this.on("drop", true) {
            it.preventDefault()

            val event = it as DragEvent?
            val target = event?.target as Element?
            val draggedFrom = event?.dataTransfer?.getData("text")

            if (draggedFrom != null) {
                val (otherKind, otherQuery, otherId) = JSON.parse<Array<String>>(draggedFrom)
                val (value, otherValue) = listOf(kind, otherKind).map { kinds[it]!! }
                val join = Join(id, otherId, query, kind, value, otherQuery, otherKind, otherValue, queries)
                join.execute()
                refreshData()
                joinRegistry.add(join)
            }
        }

        return this
    }
}

data class Join(val id: String,
           val otherId: String,
           val left: String,
           val leftKind: String,
           val leftValue: (Relation) -> String,
           val right: String,
           val rightKind: String,
           val rightValue: (Relation) -> String,
           val container: Element) {
    val ownId = "content__queries__joins__${id}_with_$otherId"

    val relation = relationFromString(left)
    val otherRelation = relationFromString(right)

    val joinContainer = createContainer(ownId, container)

    val queryContainer = createContainer("${ownId}__query", joinContainer)

    val relationSubject = createContainer("${ownId}__query__subject", queryContainer)
            .withText(relation.subject)
            .withClass("queries__query__matcher ${leftJoined("subject")}")

    val relationName = createContainer("${ownId}__query__name", queryContainer)
            .withText(relation.name)
            .withClass("queries__query__relation-name")

    val relationRelative = createContainer("${ownId}__query__relative", queryContainer)
            .withText(relation.relative)
            .withClass("queries__query__matcher ${leftJoined("relative")}")

    val otherQueryContainer = createContainer("${ownId}__query", joinContainer)

    val otherRelationSubject = createContainer("${ownId}__query__subject", otherQueryContainer)
            .withText(otherRelation.subject)
            .withClass("queries__query__matcher ${rightJoined("subject")}")

    val otherRelationName = createContainer("${ownId}__query__name", otherQueryContainer)
            .withText(otherRelation.name)
            .withClass("queries__query__relation-name")

    val otherRelationRelative = createContainer("${ownId}__query__relative", otherQueryContainer)
            .withText(otherRelation.relative)
            .withClass("queries__query__matcher ${rightJoined("relative")}")

    val resultContainer = createContainer("${ownId}__result", joinContainer)

    var cachedResult = listOf<String>()

    fun execute() {
        val newResult = join(left, leftValue, right, rightValue)

        if (newResult == cachedResult) { return }
        cachedResult = newResult

        resultContainer.textContent = newResult.joinToString(", ")
    }

    private fun leftJoined(expectedKind: String) = joined(leftKind, expectedKind)
    private fun rightJoined(expectedKind: String) = joined(rightKind, expectedKind)

    private fun joined(kind: String, expected: String): String {
        return if (kind == expected) {
            "queries__query__matcher-joined"
        } else {
            ""
        }
    }
}

fun start() {
    val queryTextElement = document.querySelector("#query__text")
    val queryAdd = document.querySelector("#query__add")
    val queries = document.querySelector("#content__queries")

    val queryText = when(queryTextElement) {
        is HTMLInputElement -> queryTextElement
        else -> throw RuntimeException("#query__text is expected to be an HTMLInputElement")
    }

    if (queries == null) {
        throw RuntimeException("#content__queries is expected to be present on page")
    }

    var queryNumber = 0

    fun addQuery(value: String) {
        queryNumber++

        val query = Query(
                value,
                "content__queries__with_id_$queryNumber",
                queries
        )

        query.execute()
        refreshData()
        queryRegistry.add(query)
    }

    queryAdd.onClick {
        addQuery(queryText.value)
    }

    listOf(
            "? is a friend of ?",
            "John is a friend of James",
            "John is a friend of Sarah",
            "John is a friend of Blake",
            "Camilla is a friend of Blake",
            "Camilla is a friend of Sarah",
            "Camilla is a friend of Ivan",
            "Ivan is a friend of Johannes",
            "John is a friend of ?",
            "Camilla is a friend of ?"
    ).forEach { addQuery(it) }
}

fun refreshData() {
    queryRegistry.forEach { it.execute() }
    joinRegistry.forEach { it.execute() }
}

private fun createContainer(id: String, appendTo: Element): Element {
    val container = document.createElement("div")
    container.id = id
    appendTo.appendChild(container)
    return container
}

private fun Element.withText(text: String): Element {
    this.textContent = text
    return this
}

private fun Element.withClass(className: String): Element {
    this.className = className
    return this
}
