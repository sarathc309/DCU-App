package com.banking.app.dcu.controller;

import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

//import javax.validation.Valid;import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.banking.app.dcu.dao.AccountNumberSequence;
import com.banking.app.dcu.dao.AccountTypeRepo;
import com.banking.app.dcu.dao.UserAccountRepo;
import com.banking.app.dcu.model.Account;
import com.banking.app.dcu.model.AccountBalance;
import com.banking.app.dcu.model.RegisterCustom;
import com.banking.app.dcu.model.UserAccount;
//import com.banking.app.dcu.model.UserInfo;
import com.banking.app.dcu.model.Users;

@Controller
@SessionAttributes("user_info")
public class AccountController {

	@Autowired
	private UserAccount account;
	
	private	 LinkedHashMap<String,String> acct_cd;
	
	@Autowired
	private AccountBalance balance;
	
//	@Autowired
//	private Users users;
	
	
	@Autowired
	private UserAccountRepo repo;
	
	@Autowired
	private AccountNumberSequence seq_repo;
	@Autowired
	private AccountTypeRepo account_repo;

	
	@RequestMapping(value="createAccount",method= RequestMethod.POST)
	public String	accountController(Model model)
	{
		model.addAttribute("user_account", account);
		model.addAttribute("account_balance", balance);
		//model.addAttribute("register",register);
		System.out.println("Inside Account Controller");
		return "CreateAccount";
	}
	
	
	@RequestMapping(value="createAccountTest",method=RequestMethod.POST)
	public String accountTestController(Model model)
	{
		 model.addAttribute("custom", new RegisterCustom(initCustomAccount()));	
		return "CreateAccount";
	}
	
	
	public LinkedHashMap<String, String> initCustomAccount()
	{
		List<Account> list=account_repo.getBankAccountNames();
		 this.acct_cd = new LinkedHashMap<String,String>();
			
			for (int i=0;i<list.size();i++)
			{
				this.acct_cd.put(list.get(i).getAccount_cd(),list.get(i).getAccount_name() );
				
			}
		 return acct_cd;
	}
	
	@RequestMapping(value="submitAccountValidation",method=RequestMethod.POST)
	public String createaccountValidatorController(@Valid @ModelAttribute("custom") RegisterCustom custom,@ModelAttribute("user_info")Users users ,Model model,
			BindingResult result,RedirectAttributes attributes)
	{
		
		String is_active=custom.getAccount().getIs_account_active();
		
		String acct_cd=custom.getAccount().getAcct_cd();
		
		
		
		if(!result.hasErrors())
		{
			if(is_active==null)
			{
				model.addAttribute("Message", "Please check the box to agree the terms and Conditions and create the Account");
				System.out.println("Check Box not Checked"+" Value is "+custom.getAccount().getIs_account_active());
				model.addAttribute("custom", new RegisterCustom(initCustomAccount()));
				
				return "CreateAccount";
			}
			
			else 
			{
				if(is_active.equals("YES"))
				{
				//System.out.println("Check Box Checked");
				
				//System.out.println(users.getUid());
				
				
				//System.out.println(users.getUid());
				
				boolean createaccount=account_repo.checkIfAccountExists(acct_cd, users)	;
				
				//System.out.println("Printing return valueeeeeeeeeeeee "+createaccount);
				
			if(createaccount==true)
			{
//				System.out.println("Printing return valueeeeeeeeeeeee "+createaccount);
				account.setAccount_no(acct_cd.concat(seq_repo.getSeqNextVal()));
				account.setAcct_cd(acct_cd);
				account.setIs_account_active(is_active);
				account.setUsers(users);
				balance.setBalance(custom.getBalance().getBalance());			
//				System.out.println("Sequence Num is ----------------------------------------------");
//				System.out.println(acct_cd + " sequence   "+seq_repo.getSeqNextVal());
// 				System.out.println(account.getAccount_no());
				repo.createUserAccount(account, balance);
				return "AccountSuccess";
			}
			
			else
			{
				model.addAttribute("Message", "There is already one "+acct_cd+" Exists for you and you can't create more than one same account type");
//				System.out.println("Check Box not Checked"+" Value is "+custom.getAccount().getIs_account_active());
				model.addAttribute("custom", new RegisterCustom(initCustomAccount()));
				return "CreateAccount";
			}
				
				
				}
				
				else
				{
					
					model.addAttribute("Message", "DOn't KNow WHat THe ERror IS");
					//System.out.println("Check Box not Checked"+" Value is "+custom.getAccount().getIs_account_active());
					model.addAttribute("custom", new RegisterCustom(initCustomAccount()));
					
					return "CreateAccount";
					
				}
				
			
			}
			
			
		}
		
		else
		{
			model.addAttribute("Message", "Errors Exist in Form Validation");
			return "CreateAccount";	
		}
		
	}
}
