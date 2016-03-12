package com.tddfellow.inference.test

import com.tddfellow.inference.query
import com.tddfellow.inference._clear
import com.tddfellow.inference.Relation

import org.junit.Assert
import org.junit.Test
import org.junit.Before

class QueryTestSuite {
    @Before fun setup() = _clear()

    @Test fun testNewRelationCreation() {
        Assert.assertEquals(
                query("? is a friend of ?"),
                listOf<Relation>()
        )
    }

    @Test fun testNewFactCreation() {
        query("? is a friend of ?")

        Assert.assertEquals(
                query("James is a friend of John"),
                listOf(Relation(" is a friend of ", "James", "John"))
        )

        Assert.assertEquals(
                query("James is a friend of Blake"),
                listOf(Relation(" is a friend of ", "James", "Blake"))
        )
    }

    @Test fun testFullQuery() {
        query("? is a friend of ?")
        query("James is a friend of John")
        query("James is a friend of Blake")
        query("John is a friend of Sarah")

        Assert.assertEquals(
                query("? is a friend of ?"),
                listOf(
                        Relation(" is a friend of ", "James", "John"),
                        Relation(" is a friend of ", "James", "Blake"),
                        Relation(" is a friend of ", "John", "Sarah")
                )
        )
    }

    @Test fun testPartialQuery() {
        query("? is a friend of ?")
        query("James is a friend of John")
        query("James is a friend of Blake")
        query("John is a friend of Sarah")

        Assert.assertEquals(
                query("James is a friend of ?"),
                listOf(
                        Relation(" is a friend of ", "James", "John"),
                        Relation(" is a friend of ", "James", "Blake")
                )
        )

        Assert.assertEquals(
                query("John is a friend of ?"),
                listOf(
                        Relation(" is a friend of ", "John", "Sarah")
                )
        )

        Assert.assertEquals(
                query("? is a friend of Sarah"),
                listOf(
                        Relation(" is a friend of ", "John", "Sarah")
                )
        )
    }

    @Test fun testMultipleRelations() {
        query("? is a friend of ?")
        query("James is a friend of John")
        query("James is a friend of Blake")
        query("John is a friend of Sarah")

        query("? works with ?")
        query("Sarah works with James")
        query("Sarah works with Bob")
        query("Alice works with Bob")

        Assert.assertEquals(
                query("James is a friend of ?"),
                listOf(
                        Relation(" is a friend of ", "James", "John"),
                        Relation(" is a friend of ", "James", "Blake")
                )
        )

        Assert.assertEquals(
                query("? works with Bob"),
                listOf(
                        Relation(" works with ", "Sarah", "Bob"),
                        Relation(" works with ", "Alice", "Bob")
                )
        )
    }
}