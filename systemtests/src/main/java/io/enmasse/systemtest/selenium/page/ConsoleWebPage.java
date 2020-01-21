/*
 * Copyright 2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.systemtest.selenium.page;

import io.enmasse.address.model.Address;
import io.enmasse.address.model.AddressSpace;
import io.enmasse.systemtest.UserCredentials;
import io.enmasse.systemtest.logs.CustomLogger;
import io.enmasse.systemtest.model.addressspace.AddressSpaceType;
import io.enmasse.systemtest.selenium.SeleniumProvider;
import io.enmasse.systemtest.selenium.resources.AddressSpaceWebItem;
import io.enmasse.systemtest.selenium.resources.AddressWebItem;
import io.enmasse.systemtest.utils.AddressSpaceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ConsoleWebPage implements IWebPage {

    private static Logger log = CustomLogger.getLogger();

    SeleniumProvider selenium;
    String ocRoute;
    UserCredentials credentials;
    OpenshiftLoginWebPage loginPage;

    public ConsoleWebPage(SeleniumProvider selenium, String ocRoute, UserCredentials credentials) {
        this.selenium = selenium;
        this.ocRoute = ocRoute;
        this.credentials = credentials;
        this.loginPage = new OpenshiftLoginWebPage(selenium);
    }

    //================================================================================================
    // Getters and finders of elements and data
    //================================================================================================
    private WebElement getContentElem() {
        return selenium.getDriver().findElement(By.id("main-container"));
    }

    private WebElement getCreateAddressSpaceButtonTop() {
        return selenium.getDriver().findElement(By.id("al-filter-overflow-button"));
    }

    private WebElement getCreateAddressSpaceButtonEmptyPage() {
        return selenium.getDriver().findElement(By.id("empty-ad-space-create-button"));
    }

    private WebElement getAddressSpaceTable() {
        return selenium.getDriver().findElement(By.xpath("//table[@aria-label='address space list']"));
    }

    private WebElement getAddressSpaceHeader() {
        return getAddressSpaceTable().findElement(By.id("aslist-table-header"));
    }

    private WebElement getAddressSpaceList() {
        return getAddressSpaceTable().findElement(By.tagName("tbody"));
    }

    private WebElement getAddressTable() {
        return selenium.getDriver().findElement(By.xpath("//table[@aria-label='Address List']"));
    }

    private WebElement getAddressHeader() {
        return getAddressSpaceTable().findElement(By.id("aslist-table-header"));
    }

    private WebElement getAddressList() {
        return getAddressTable().findElement(By.tagName("tbody"));
    }

    public List<AddressSpaceWebItem> getAddressSpaceItems() {
        List<WebElement> elements = getAddressSpaceList().findElements(By.tagName("tr"));
        List<AddressSpaceWebItem> addressSpaceItems = new ArrayList<>();
        for (WebElement element : elements) {
            AddressSpaceWebItem addressSpace = new AddressSpaceWebItem(element);
            log.info(String.format("Got addressSpace: %s", addressSpace.toString()));
            addressSpaceItems.add(addressSpace);
        }
        return addressSpaceItems;
    }

    public AddressSpaceWebItem getAddressSpaceItem(AddressSpace as) {
        AddressSpaceWebItem returnedElement = null;
        List<AddressSpaceWebItem> addressWebItems = getAddressSpaceItems();
        for (AddressSpaceWebItem item : addressWebItems) {
            if (item.getName().equals(as.getMetadata().getName()) && item.getNamespace().equals(as.getMetadata().getNamespace()))
                returnedElement = item;
        }
        return returnedElement;
    }

    public List<AddressWebItem> getAddressItems() {
        List<WebElement> elements = getAddressList().findElements(By.tagName("tr"));
        List<AddressWebItem> addressSpaceItems = new ArrayList<>();
        for (WebElement element : elements) {
            AddressWebItem address = new AddressWebItem(element);
            log.info(String.format("Got address: %s", address.toString()));
            addressSpaceItems.add(address);
        }
        return addressSpaceItems;
    }

    public AddressWebItem getAddressItem(Address as) {
        AddressWebItem returnedElement = null;
        List<AddressWebItem> addressWebItems = getAddressItems();
        for (AddressWebItem item : addressWebItems) {
            if (item.getName().equals(as.getMetadata().getName()))
                returnedElement = item;
        }
        return returnedElement;
    }

    //////////////////////////////////////////////////////////////////////
    private WebElement getDeleteAllButton() {
        return getContentElem().findElement(By.id("al-filter-overflow-dropdown"))
                .findElement(By.xpath("./button[contains(text(), 'Delete All')]"));
    }

    private WebElement getDeleteButton() {
        return selenium.getDriver().findElement(By.id("dd-menuitem-delete"));
    }

    private WebElement getNamespaceDropDown() {
        return selenium.getDriver().findElement(By.id("cas-dropdown-namespace"));
    }

    private WebElement getAuthServiceDropDown() {
        return selenium.getDriver().findElement(By.id("cas-dropdown-auth-service"));
    }

    private WebElement getAddressSpaceNameInput() {
        return selenium.getDriver().findElement(By.id("address-space"));
    }

    private WebElement getBrokeredRadioButton() {
        return selenium.getDriver().findElement(By.id("cas-brokered-radio"));
    }

    private WebElement getStandardRadioButton() {
        return selenium.getDriver().findElement(By.id("cas-standard-radio"));
    }

    private WebElement getPlanDropDown() {
        return selenium.getDriver().findElement(By.id("cas-dropdown-plan"));
    }

    private WebElement getNextButton() {
        return selenium.getDriver().findElement(By.xpath("//button[contains(text(), 'Next')]"));
    }

    private WebElement getCancelButton() {
        return selenium.getDriver().findElement(By.xpath("//button[contains(text(), 'Cancel')]"));
    }

    private WebElement getFinishButton() {
        return selenium.getDriver().findElement(By.xpath("//button[contains(text(), 'Finish')]"));
    }

    private WebElement getBackButton() {
        return selenium.getDriver().findElement(By.xpath("//button[contains(text(), 'Back')]"));
    }

    private WebElement getModalButtonDelete() {
        return selenium.getDriver().findElement(By.xpath("//button[contains(text(), 'Confirm')]"));
    }


    //================================================================================================
    // Operations
    //================================================================================================

    public void openGlobalConsolePage() throws Exception {
        log.info("Opening global console on route {}", ocRoute);
        selenium.getDriver().get(ocRoute);
        if (waitUntilLoginPage()) {
            selenium.getAngularDriver().waitForAngularRequestsToFinish();
            selenium.takeScreenShot();
            try {
                logout();
            } catch (Exception ex) {
                log.info("User is not logged");
            }
            if (!login())
                throw new IllegalAccessException(loginPage.getAlertMessage());
        }
        selenium.getAngularDriver().waitForAngularRequestsToFinish();
        if (!waitUntilConsolePage()) {
            throw new IllegalStateException("Openshift console not loaded");
        }
    }

    public void openAddressList(AddressSpace addressSpace) throws Exception {
        AddressSpaceWebItem item = selenium.waitUntilItemPresent(30, () -> getAddressSpaceItem(addressSpace));
        selenium.clickOnItem(item.getConsoleRoute());
        selenium.getWebElement(this::getAddressList);
    }

    private void selectNamespace(String namespace) throws Exception {
        selenium.clickOnItem(getNamespaceDropDown(), "namespace dropdown");
        selenium.clickOnItem(selenium.getDriver().findElement(By.xpath("//button[@value='" + namespace + "']")), namespace);
    }

    private void selectPlan(String plan) throws Exception {
        selenium.clickOnItem(getPlanDropDown(), "address space plan dropdown");
        selenium.clickOnItem(selenium.getDriver().findElement(By.xpath("//button[@value='" + plan + "']")), plan);
    }

    private void selectAuthService(String authService) throws Exception {
        selenium.clickOnItem(getAuthServiceDropDown(), "address space plan dropdown");
        selenium.clickOnItem(selenium.getDriver().findElement(By.xpath("//button[@value='" + authService + "']")), authService);
    }

    public void createAddressSpace(AddressSpace addressSpace) throws Exception {
        selenium.clickOnItem(getCreateAddressSpaceButtonTop());
        selectNamespace(addressSpace.getMetadata().getNamespace());
        selenium.fillInputItem(getAddressSpaceNameInput(), addressSpace.getMetadata().getName());
        selenium.clickOnItem(addressSpace.getSpec().getType().equals(AddressSpaceType.BROKERED.toString().toLowerCase()) ? getBrokeredRadioButton() : getStandardRadioButton(),
                addressSpace.getSpec().getType());
        selectPlan(addressSpace.getSpec().getPlan());
        selectAuthService(addressSpace.getSpec().getAuthenticationService().getName());
        selenium.clickOnItem(getNextButton());
        selenium.clickOnItem(getFinishButton());
        selenium.waitUntilItemPresent(30, () -> getAddressSpaceItem(addressSpace));
        selenium.takeScreenShot();
        AddressSpaceUtils.waitForAddressSpaceReady(addressSpace);
        selenium.refreshPage();
    }

    public void deleteAddressSpace(AddressSpace addressSpace) throws Exception {
        AddressSpaceWebItem item = selenium.waitUntilItemPresent(30, () -> getAddressSpaceItem(addressSpace));
        selenium.clickOnItem(item.getActionDropDown(), "Address space dropdown");
        selenium.clickOnItem(item.getDeleteMenuItem());
        selenium.clickOnItem(getModalButtonDelete());
        selenium.waitUntilItemNotPresent(30, () -> getAddressSpaceItem(addressSpace));
    }

    public void switchAddressSpacePlan(AddressSpace addressSpace, String addressSpacePlan) {
        selenium.clickOnItem(getAddressSpaceItem(addressSpace).getActionDropDown());
        selenium.clickOnItem(selenium.getDriver().findElement(By.xpath("//a[contains(text(), 'Edit')]")));
        selenium.clickOnItem(selenium.getDriver().findElement(By.id("form-planName")));
        selenium.clickOnItem(selenium.getDriver()
                .findElement(By.xpath("//option[@value='" + addressSpacePlan + "']")));
        selenium.clickOnItem(selenium.getDriver().findElement(By.id("button-edit-save")));
        selenium.refreshPage();
        addressSpace.getSpec().setPlan(addressSpacePlan);
    }

    //================================================================================================
    // Login
    //================================================================================================

    private boolean login() throws Exception {
        return loginPage.login(credentials.getUsername(), credentials.getPassword());
    }

    public void logout() {
        try {
            WebElement userDropdown = selenium.getDriver().findElement(By.id("dd-user"));
            selenium.clickOnItem(userDropdown, "User dropdown navigation");
            WebElement logout = selenium.getDriver().findElement(By.id("dd-menuitem-logout"));
            selenium.clickOnItem(logout, "Log out");
        } catch (Exception ex) {
            log.info("Unable to logout, user is not logged in");
        }
    }

    private boolean waitUntilLoginPage() {
        try {
            selenium.getDriverWait().withTimeout(Duration.ofSeconds(3)).until(ExpectedConditions.titleContains("Log"));
            selenium.clickOnItem(selenium.getDriver().findElement(By.tagName("button")));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean waitUntilConsolePage() {
        try {
            selenium.getDriverWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("root")));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void checkReachableWebPage() {
        selenium.getDriverWait().withTimeout(Duration.ofSeconds(60)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("root")),
                ExpectedConditions.titleContains("Address Space List")
        ));
    }
}
