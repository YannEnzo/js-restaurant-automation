/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import Model.User;

/**
 * Interface to be implemented by controllers that need user information
 */
public interface UserAwareController {
    /**
     * Set the current user for this controller
     * @param user User object
     */
    void setCurrentUser(User user);
}
