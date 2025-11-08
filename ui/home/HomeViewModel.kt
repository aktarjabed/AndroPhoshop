package com.aktarjabed.androphoshop.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BackgroundRemove
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.androphoshop.data.model.Project
import com.aktarjabed.androphoshop.data.model.QuickAction
import com.aktarjabed.androphoshop.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _recentProjects = mutableStateOf<List<Project>>(emptyList())
    val recentProjects: State<List<Project>> get() = _recentProjects

    private val _quickActions = mutableStateOf<List<QuickAction>>(emptyList())
    val quickActions: State<List<QuickAction>> get() = _quickActions

    init {
        loadQuickActions()
        loadRecentProjects()
    }

    private fun loadQuickActions() {
        _quickActions.value = listOf(
            QuickAction("enhance", "Enhance", Icons.Default.AutoAwesome) { navController ->
                // Navigate to enhance
            },
            QuickAction("remove_bg", "Remove BG", Icons.Default.BackgroundRemove) { navController ->
                // Navigate to remove background
            },
            QuickAction("replace_bg", "Replace BG", Icons.Default.Landscape) { navController ->
                // Navigate to replace background
            },
            QuickAction("retouch", "Magic Retouch", Icons.Default.Healing) { navController ->
                // Navigate to retouch
            }
        )
    }

    private fun loadRecentProjects() {
        viewModelScope.launch {
            projectRepository.getRecentProjects().collect { projects ->
                _recentProjects.value = projects
            }
        }
    }
}