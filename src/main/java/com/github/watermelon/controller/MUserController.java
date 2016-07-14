package com.github.watermelon.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.watermelon.entity.MUser;
import com.github.watermelon.service.MUserService;
import com.github.watermelon.vo.ResultVO;
import com.github.watermelon.vo.SysDefinition;

@Controller
@RequestMapping("/user")
public class MUserController {
	/**
	 * logger.
	 */
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private MUserService mUserService;
	
	/**
	 * 查询信息
	 * @param username
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/get")
	public ResultVO<MUser> get(String userName){
		MUser result=null;
		try{
			Query<MUser> query = mUserService.createQuery().filter("userName",userName);
			result=mUserService.findOne(query);
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ResultVO<MUser>(SysDefinition.CODE_ERROR,null,null);
		}
		if(result!=null){
			return new ResultVO<MUser>(SysDefinition.CODE_SUCCESS,null,result);
		}else{
			return new ResultVO<MUser>(SysDefinition.CODE_NODATA,null,null);
		}
	}
	
	/**
	 * 新增用户
	 * @param username
	 * @param password
	 * @param nickname
	 * @param email
	 * @return
	 */
	@RequestMapping(value="/register")
	public ModelAndView register(HttpSession session, HttpServletResponse response, MUser param){
		MUser result=null;
		try{
			result=mUserService.register(param);
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ModelAndView("redirect:/signin");
		}
		if(result!=null){
			session.setAttribute(SysDefinition.USER_SESSION_KEY, result);
			saveCookie(session, result, response);
			return new ModelAndView("redirect:/home");
		}else{
			return new ModelAndView("redirect:/signin");
		}
	}
	
	/**
	 * 登录
	 * @param session
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/login")
	public ModelAndView login(HttpSession session, HttpServletResponse response, MUser param){
		MUser result=null;
		try{
			result=mUserService.login(param);
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ModelAndView("redirect:/signin");
		}
		if(result!=null){
			session.setAttribute(SysDefinition.USER_SESSION_KEY, result);
			saveCookie(session, result, response);
			return new ModelAndView("redirect:/home");
		}else{
			return new ModelAndView("redirect:/signin");
		}
	}
	
	private void saveCookie(HttpSession session, MUser muser, HttpServletResponse response){
		Cookie sessionCookie = new Cookie(SysDefinition.COOKIE_SESSIONID_KEY, session.getId());
		sessionCookie.setMaxAge(3600);
		sessionCookie.setPath("/");		
		response.addCookie(sessionCookie);
		
		Cookie loginidCookie = new Cookie(SysDefinition.COOKIE_LOGINID_KEY, muser.getUserName());
		//30*24*60*60
		loginidCookie.setMaxAge(2592000);
		loginidCookie.setPath("/");		
		response.addCookie(loginidCookie);
	}
	
	/**
	 * 登出
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/logout")
	public ResultVO<Object> logout(HttpSession session){
		session.removeAttribute(SysDefinition.USER_SESSION_KEY);
		return new ResultVO<Object>(SysDefinition.CODE_SUCCESS,null,null);
	}
	
	/**
	 * 删除当前账号, 删除必须是登录状态, 且需要提交密码验证身份
	 * @param session
	 * @param password
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/delete")
	public ResultVO<MUser> delete(HttpSession session, String password){
		MUser result= (MUser)session.getAttribute(SysDefinition.USER_SESSION_KEY);
		if(result==null){
			return new ResultVO<MUser>(SysDefinition.CODE_NODATA,null,null);
		}
		try{
			mUserService.delete(result, password);
			session.removeAttribute(SysDefinition.USER_SESSION_KEY);
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ResultVO<MUser>(SysDefinition.CODE_ERROR,null,null);
		}
		return new ResultVO<MUser>(SysDefinition.CODE_SUCCESS,null,null);
	}
	
	@ResponseBody
	@RequestMapping(value="/update/signature")
	public ResultVO<String> signature(HttpSession session, String content){
		MUser result= (MUser)session.getAttribute(SysDefinition.USER_SESSION_KEY);
		if(result==null){
			return new ResultVO<String>(SysDefinition.CODE_NODATA,null,null);
		}
		try{
			mUserService.signature(result, content);
			session.setAttribute(SysDefinition.USER_SESSION_KEY, result);
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ResultVO<String>(SysDefinition.CODE_ERROR,null,null);
		}
		return new ResultVO<String>(SysDefinition.CODE_SUCCESS,null,content);
	}
	
	@ResponseBody
	@RequestMapping(value="/follow")
	public ResultVO<String> follow(HttpSession session, String name){
		MUser result= (MUser)session.getAttribute(SysDefinition.USER_SESSION_KEY);
		if(result==null){
			return new ResultVO<String>(SysDefinition.CODE_NODATA,null,null);
		}
		try{
			if(mUserService.follow(result, name)){
				return new ResultVO<String>(SysDefinition.CODE_SUCCESS,null,null);
			}
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ResultVO<String>(SysDefinition.CODE_ERROR,null,null);
		}
		return new ResultVO<String>(SysDefinition.CODE_NODATA,null,null);
	}
	
	@ResponseBody
	@RequestMapping(value="/unfollow")
	public ResultVO<String> unfollow(HttpSession session, String name){
		MUser result= (MUser)session.getAttribute(SysDefinition.USER_SESSION_KEY);
		if(result==null){
			return new ResultVO<String>(SysDefinition.CODE_NODATA,null,null);
		}
		try{
			if(mUserService.unfollow(result, name)){
				return new ResultVO<String>(SysDefinition.CODE_SUCCESS,null,null);
			}
		}catch(Exception e){
			logger.error(e.getMessage());
			return new ResultVO<String>(SysDefinition.CODE_ERROR,null,null);
		}
		return new ResultVO<String>(SysDefinition.CODE_NODATA,null,null);
	}
	

}
