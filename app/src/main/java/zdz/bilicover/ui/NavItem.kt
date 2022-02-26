package zdz.bilicover.ui

sealed class NavItem(val route: String) {
    object MainScr : NavItem("MAIN")
    object GuideScr : NavItem("GUIDE")
}