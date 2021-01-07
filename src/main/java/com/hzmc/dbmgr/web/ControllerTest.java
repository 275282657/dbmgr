package com.hzmc.dbmgr.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ControllerTest {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public void test() {
        
    	System.out.println("This is test method.");
    	
    }


}
