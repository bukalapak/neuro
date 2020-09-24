package com.bukalapak.neuro

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AxonBranchSortingTest {

    @Test
    fun `axon branch sorting of a terminal is correct`() {

        val branch1 = AxonBranch("/help") {}
        val branch2 = AxonBranch("/faq") {}
        val branch3 = AxonBranch("help", "/help", 99) {}
        val branch4 = AxonBranch("/helpme") {}
        val branch5 = AxonBranch("/welp") {}
        val branch6 = AxonBranch("belt", "/belt") {}
        val branch7 = AxonBranch("/belt") {}
        val branch8 = AxonBranch("bell", "/belt") {}

        assert(branch1 < branch2)
        assert(branch1 > branch3)
        assert(branch1 > branch4)
        assert(branch1 < branch5)
        assert(branch1 > branch6)
        assert(branch1 > branch7)
        assert(branch1 > branch8)

        assert(branch2 > branch3)
        assert(branch2 > branch4)
        assert(branch2 > branch5)
        assert(branch2 > branch6)
        assert(branch2 > branch7)
        assert(branch2 > branch8)

        assert(branch3 < branch4)
        assert(branch3 < branch5)
        assert(branch3 < branch6)
        assert(branch3 < branch7)
        assert(branch3 < branch8)

        assert(branch4 < branch5)
        assert(branch4 < branch6)
        assert(branch4 < branch7)
        assert(branch4 < branch8)

        assert(branch5 > branch6)
        assert(branch5 > branch7)
        assert(branch5 > branch8)

        assert(branch6 < branch7)
        assert(branch6 > branch8)

        assert(branch7 > branch8)
    }
}