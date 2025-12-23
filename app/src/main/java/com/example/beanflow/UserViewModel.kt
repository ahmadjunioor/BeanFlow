package com.example.beanflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.User
import com.example.beanflow.data.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {

    val cashierList: StateFlow<List<User>> = userDao.getAllCashiers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCashier(username: String, password: String) {
        viewModelScope.launch {
            val newUser = User(
                username = username,
                password = password,
                role = "Cashier"
            )
            // Pastikan ini memanggil 'insertUser', bukan 'insert'
            userDao.insertUser(newUser)
        }
    }

    fun deleteCashier(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }
}