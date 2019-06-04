package com.gorilla.vc

import org.hamcrest.core.IsCollectionContaining.hasItem
import org.junit.Test

import org.junit.Assert.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testBasic() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)

        mockedList.add("one")
        mockedList.clear()

        Mockito.verify(mockedList).add("one")
        Mockito.verify(mockedList).clear()
    }

    @Test
    fun verifySimpleInvocation() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        mockedList.size
        verify(mockedList).size
    }

    @Test
    fun verifyNumberInvocation() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        mockedList.size
        mockedList.size
        verify(mockedList, times(2)).size
    }

    @Test
    fun verifyNoInvocation() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        verifyZeroInteractions(mockedList)
    }

    @Test
    fun verifyOrderOfInteractions() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        mockedList.size
        mockedList.add("parameter one")
        mockedList.clear()

        val inOrder = Mockito.inOrder(mockedList)
        inOrder.verify(mockedList).size
        inOrder.verify(mockedList).add("parameter one")
        inOrder.verify(mockedList).clear()
    }

    @Test
    fun verifyInvocationNotOccur() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        mockedList.size
        verify(mockedList, never()).clear()
    }

    @Test
    fun verifyInvocationFlexible() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)
        mockedList.add("parameter one")
        mockedList.add("parameter two")
        mockedList.add("parameter three")
        verify(mockedList, times(3)).add(ArgumentMatchers.anyString())
    }

    private inline fun <reified T : Any> argumentCaptor() = ArgumentCaptor.forClass(T::class.java)
    @Test
    fun verifyArgumentCapture() {
        val mockedList = Mockito.mock(mutableListOf<String>().javaClass)

        val newArrayList = mutableListOf("element 1", "element 2")
        mockedList.addAll(newArrayList)

        val argumentCaptor = argumentCaptor<Collection<String>>()
        verify(mockedList).addAll(argumentCaptor.capture())

        val capturedArgument = argumentCaptor.value
        assertThat(capturedArgument, hasItem("element 1"))
    }

    @Test
    fun verifyArgumentCapture2() {
        val list1 = Mockito.mock(mutableListOf<String>().javaClass)
        val list2 = Mockito.mock(mutableListOf<String>().javaClass)

        list1.add("Item One")
        list2.add("Item Two")
        list2.add("Item Three")

        val argumentCaptor = argumentCaptor<String>()
        verify(list1).add(argumentCaptor.capture())
        assertEquals("Item One", argumentCaptor.value)

        verify(list2, times(2)).add(argumentCaptor.capture())
        assertEquals("Item Two", argumentCaptor.value)
        assertArrayEquals(arrayOf("Item Two", "Item Three"), argumentCaptor.allValues.toTypedArray())
    }
}
