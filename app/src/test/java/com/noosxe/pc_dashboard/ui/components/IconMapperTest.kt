package com.noosxe.pc_dashboard.ui.components

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IconMapperTest {

    @Test
    fun testGetSimpleIcon_Success() {
        assertNotNull(IconMapper.getSimpleIcon("discord"))
        assertNotNull(IconMapper.getSimpleIcon("spotify"))
        assertNotNull(IconMapper.getSimpleIcon("vscode"))
        assertNotNull(IconMapper.getSimpleIcon("materialgram"))
        assertNotNull(IconMapper.getSimpleIcon("intellij-idea"))
        assertNotNull(IconMapper.getSimpleIcon("android-studio"))
        assertNotNull(IconMapper.getSimpleIcon("vlc"))
        assertNotNull(IconMapper.getSimpleIcon("x"))
    }

    @Test
    fun testGetSimpleIcon_Failure() {
        assertNull(IconMapper.getSimpleIcon("non-existent-app"))
    }
    
    @Test
    fun testGetSimpleIcon_CaseInsensitive() {
        assertNotNull(IconMapper.getSimpleIcon("Discord"))
        assertNotNull(IconMapper.getSimpleIcon("SPOTIFY"))
    }
}
