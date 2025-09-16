package my.app

enum class UserType {
    ADMIN,
    USER,
    GUEST,
}

data class Item(val name: String)

data class User(val name: String, val type: UserType)
