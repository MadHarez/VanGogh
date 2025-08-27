package com.hy.vangogh.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hy.vangogh.data.model.Project

class ProjectRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vangogh_projects", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()
    
    init {
        loadProjects()
    }
    
    fun createProject(name: String, description: String = ""): Project {
        val project = Project(
            name = name,
            description = description
        )
        
        val updatedProjects = _projects.value + project
        _projects.value = updatedProjects
        saveProjects()
        
        return project
    }
    
    fun selectProject(project: Project) {
        _currentProject.value = project
        saveCurrentProjectId(project.id)
    }
    
    fun updateProject(updatedProject: Project) {
        val updatedProjects = _projects.value.map { project ->
            if (project.id == updatedProject.id) {
                updatedProject.copy(lastModified = System.currentTimeMillis())
            } else {
                project
            }
        }
        _projects.value = updatedProjects
        
        if (_currentProject.value?.id == updatedProject.id) {
            _currentProject.value = updatedProject.copy(lastModified = System.currentTimeMillis())
        }
        
        saveProjects()
    }
    
    fun deleteProject(projectId: String) {
        val updatedProjects = _projects.value.filter { it.id != projectId }
        _projects.value = updatedProjects
        
        if (_currentProject.value?.id == projectId) {
            _currentProject.value = null
            clearCurrentProjectId()
        }
        
        saveProjects()
    }
    
    fun addImageToProject(projectId: String, imageUri: Uri): Project? {
        val project = _projects.value.find { it.id == projectId } ?: return null
        val updatedProject = project.copy(
            originalImageUri = imageUri,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
        return updatedProject
    }
    
    private fun saveProjects() {
        val projectsJson = gson.toJson(_projects.value)
        prefs.edit().putString("projects", projectsJson).apply()
    }
    
    private fun loadProjects() {
        val projectsJson = prefs.getString("projects", null)
        if (projectsJson != null) {
            try {
                val type = object : TypeToken<List<Project>>() {}.type
                val loadedProjects = gson.fromJson<List<Project>>(projectsJson, type)
                _projects.value = loadedProjects
                
                // Load current project
                val currentProjectId = prefs.getString("current_project_id", null)
                if (currentProjectId != null) {
                    _currentProject.value = loadedProjects.find { it.id == currentProjectId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _projects.value = emptyList()
            }
        }
    }
    
    private fun saveCurrentProjectId(projectId: String) {
        prefs.edit().putString("current_project_id", projectId).apply()
    }
    
    private fun clearCurrentProjectId() {
        prefs.edit().remove("current_project_id").apply()
    }
}
