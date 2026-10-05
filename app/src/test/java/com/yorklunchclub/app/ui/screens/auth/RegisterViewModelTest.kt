package com.yorklunchclub.app.ui.screens.auth

import com.yorklunchclub.app.data.repository.AuthRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(authRepository = mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tc07_usernameValidation_emptyFullName_showsError() {
        viewModel.onFullNameChange("")
        viewModel.onUsernameChange("testuser")
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.register()
        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Full name is required.", state.error)
    }

    @Test
    fun tc07_usernameValidation_emptyUsername_showsError() {
        viewModel.onFullNameChange("Test User")
        viewModel.onUsernameChange("")
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.register()
        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Username is required.", state.error)
    }

    @Test
    fun tc07_usernameValidation_emptyEmail_showsError() {
        viewModel.onFullNameChange("Test User")
        viewModel.onUsernameChange("testuser")
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.register()
        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Email is required.", state.error)
    }

    @Test
    fun tc08_passwordMatchValidation_mismatchedPasswords_showsError() {
        viewModel.onFullNameChange("Test User")
        viewModel.onUsernameChange("testuser")
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("different456")
        viewModel.register()
        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Passwords do not match.", state.error)
    }

    @Test
    fun tc08_passwordMatchValidation_shortPassword_showsError() {
        viewModel.onFullNameChange("Test User")
        viewModel.onUsernameChange("testuser")
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")
        viewModel.register()
        val state = viewModel.state.value
        assertNotNull(state.error)
        assertEquals("Password must be 6+ characters.", state.error)
    }
}
