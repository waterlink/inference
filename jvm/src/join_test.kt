package com.tddfellow.inference.test

import com.tddfellow.inference.query
import com.tddfellow.inference.join
import com.tddfellow.inference._clear
import com.tddfellow.inference.Relation

import org.junit.Assert
import org.junit.Test
import org.junit.Before

class JoinTestSuite {
    @Before fun setup() = _clear()

    @Test fun testFindCommonFriends() {
        query("? is a friend of ?")
        query("John is a friend of Bruce")
        query("John is a friend of Camila")
        query("John is a friend of Max")
        query("Blake is a friend of Camila")
        query("Sarah is a friend of Max")
        query("Sarah is a friend of Blake")
        query("Sarah is a friend of Camila")

        Assert.assertEquals(
                join(
                        query("John is a friend of ?"), { it.relative },
                        query("Sarah is a friend of ?"), { it.relative }
                ),
                listOf("Camila", "Max")
        )

        Assert.assertEquals(
                join(
                        "John is a friend of ?",
                        "Sarah is a friend of ?"
                ),
                listOf("Camila", "Max")
        )

        Assert.assertEquals(
                join(
                        "? is a friend of Camila",
                        "? is a friend of Max"
                ),
                listOf("John", "Sarah")
        )

        Assert.assertEquals(
                join(
                        "? is a friend of Camila",
                        "Sarah is a friend of ?"
                ),
                listOf("Blake")
        )

        Assert.assertEquals(
                join(
                        "? is a friend of ?", { it.relative },
                        "? is a friend of ?", { it.subject }
                ),
                listOf("Blake")
        )
    }
}
