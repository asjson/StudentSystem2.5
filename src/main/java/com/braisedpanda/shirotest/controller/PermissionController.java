package com.braisedpanda.shirotest.controller;

import com.braisedpanda.shirotest.bean.*;
import com.braisedpanda.shirotest.service.PermissionService;
import com.braisedpanda.shirotest.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class PermissionController {

    @Autowired
    PermissionService permissionService;
    @Autowired
    UserService userService;

    @RequestMapping("topermission")
    public String topermission(){
        return null;
    }

    //新增角色
    @RequestMapping("addrole")
    public String addrole(Role role, HttpServletRequest request,Model model){
        //添加角色信息

        String roleId = UUID.randomUUID()+"";
        roleId = roleId.replace("-","");
        role.setroleId(roleId);

        permissionService.addRole(role);

        String[] s = request.getParameterValues("permission");
        for (String s1:
             s) {
           Permission permission =  permissionService.getPermissionById(s1);
            RolePermission rp = new RolePermission();
            String rPId = UUID.randomUUID()+"";
            rPId = rPId.replace("-","");
            rp.setrPId(rPId);
            rp.setnationName(permission.getnationName());
            rp.setPermission(permission.getPermission());
            rp.setpermissionDescribe(permission.getpermissionDescribe());
            rp.setroleId(roleId);
            rp.setRole(role.getRole());
            rp.setroleDescribe(role.getroleDescribe());

            permissionService.addRole_Permission(rp);



        }
        model.addAttribute("msg","添加角色成功");
        return "menu/msg";
    }

    //跳转到授予权限的界面
    @RequestMapping("toaddpermission")
    public ModelAndView  toaddpermission(){
        ModelAndView modelAndView = new ModelAndView();
        List<Role> roleList = permissionService.getAllRole();
        modelAndView.addObject(roleList);
        modelAndView.setViewName("permission/addpermission");
        return modelAndView;
    }

    //根据uid查找所对应的角色
    @ResponseBody
    @RequestMapping("findrolebyid/{uid}")
    @Cacheable(value="permission" , key="#uid")
    public List<UserRole> findrolebyid(@PathVariable("uid") String uid, Model model){
        List<UserRole> user_RoleList = permissionService.getRoleById(uid);
        User user = userService.getUserByUid(Integer.parseInt(uid));
       if(user_RoleList !=null && user_RoleList.size()>0){
           model.addAttribute("roleList", user_RoleList);
       }else if(user!=null){ //用户存在，但是没有角色
           UserRole role = new UserRole();
           role.setRole("无");
           role.setUsername(user.getUsername());
           user_RoleList.add(role);
           model.addAttribute("roleList", user_RoleList);
       }else{  //用户名和角色都不存在
           UserRole role = new UserRole();
           role.setRole("无");
           role.setUsername("无");
           user_RoleList.add(role);
           model.addAttribute("roleList", user_RoleList);
       }



        return user_RoleList;
    }


    //新增权限
    @RequestMapping("addpermission")
    public String addpermission(int uid,HttpServletRequest request,Model model){
            User user = userService.getUserByUid(uid);

            if(user == null){
                model.addAttribute("msg","**该用户不存在，请检查id是否输入正确");
                List<Role> roleList = permissionService.getAllRole();
                model.addAttribute(roleList);
                return "permission/addpermission";
            }
            //先清空原先有的数据,在表user_role中
            permissionService.deleteRoleByUid(uid);
            //在user_role中，添加相关的数据
            String[] roles = request.getParameterValues("role");
            if(roles ==null || roles.length==0){
                model.addAttribute("msg","***请选择至少一个角色");
                List<Role> roleList = permissionService.getAllRole();
                model.addAttribute(roleList);
                return "permission/addpermission";
            }else
            for (String roleId:
                    roles) {
               Role role = permissionService.getRoleByroleId(roleId);
               String u_r_id = UUID.randomUUID()+"";
               u_r_id = u_r_id.replace("-","");
               UserRole user_role = new UserRole();
               user_role.setU_r_id(u_r_id);
               user_role.setroleDescribe(role.getroleDescribe());
               user_role.setRole(role.getRole());
               user_role.setUid(user.getUid());
               user_role.setUsername(user.getUsername());
               user_role.setroleId(role.getroleId());
               permissionService.addUser_Role(user_role);

            }
        model.addAttribute("msg","操作成功");

        return "menu/msg";
    }

    //查询表role_permission表中所有内容
    @RequestMapping("allpermission")
    public @ResponseBody Map<String,Object> allpermission(int page,int limit){


        List<RolePermission> role_permissionList = permissionService.getAllRole_Permission();
        int count = role_permissionList.size();
        PageHelper.startPage(page,limit);

        List<RolePermission> role_permissionList1 = permissionService.getAllRole_Permission();

        PageInfo<RolePermission> role_permissionPageInfo = new PageInfo<>(role_permissionList1);

        List<RolePermission> role_permissionlist = role_permissionPageInfo.getList();

        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("code",0);
        resultMap.put("msg","");
        resultMap.put("count",count);


        resultMap.put("data",role_permissionlist);
        return resultMap;

    }

    //删除role_permission中，角色所有的权限
    @ResponseBody
    @RequestMapping("permission/delete/{rPId}")
    public void deleteRolePermissionById(@PathVariable("rPId")String rPId){
        permissionService.deleteRolePermissionById(rPId);
    }

    //编辑角色对应的权限
    @RequestMapping("permission/toedit/{roleId}")
    public ModelAndView permission_toedit(@PathVariable("roleId")String roleId){
        ModelAndView modelAndView = new ModelAndView();
        List<RolePermission> role_permissionlist =  permissionService.getRole_PermissionById(roleId);
        List<Permission> permissionList= permissionService.getAllPermission();
        modelAndView.addObject("permissionList",permissionList);

       modelAndView.addObject("role_permission",role_permissionlist.get(0));

       modelAndView.setViewName("permission/editrole_permission");
       return modelAndView;
    }

    //编辑角色权限信息
    @RequestMapping("editrole_permission")
    public String edit_role_perission(RolePermission role_permission, HttpServletRequest request, Model model ){
        //1、根据roleId删除原先角色所有的权限
        String roleId = role_permission.getroleId();

        permissionService.deleteRolePermissionByroleId(roleId);

        //2、根据表单内容添加角色对应的权限

        String[] permissionList = request.getParameterValues("permission");

        for (String s1:
                permissionList) {
            Permission permission =  permissionService.getPermissionById(s1);
            RolePermission role_permission1 = new RolePermission();
            String rPId = UUID.randomUUID()+"";
            rPId = rPId.replace("-","");
            role_permission1.setrPId(rPId);
            role_permission1.setnationName(permission.getnationName());
            role_permission1.setPermission(permission.getPermission());
            role_permission1.setpermissionDescribe(permission.getpermissionDescribe());
            role_permission1.setroleId(roleId);
            role_permission1.setRole(role_permission.getRole());
            role_permission1.setroleDescribe(role_permission.getroleDescribe());
            System.out.println("role_permission:"+role_permission1);
            //在role表中更新role的相关权限、名称等信息
            Role role = new Role();
            role.setroleId(roleId);
            role.setroleDescribe(role_permission.getroleDescribe());
            role.setRole(role_permission.getRole());
            permissionService.updateRole(role);

            permissionService.addRole_Permission(role_permission1);
        }
        
       model.addAttribute("msg","修改用户权限成功");
        
        return "menu/msg";

    }
    //跳转到新增角色权限表
    @RequestMapping("torole")
    public ModelAndView torole(){
        ModelAndView modelAndView = new ModelAndView();
        List<Permission> permissionList= permissionService.getAllPermission();
        modelAndView.addObject("permissionList",permissionList);

        modelAndView.setViewName("permission/role");
        return modelAndView;
    }

    //查询表role表中所有内容
    @RequestMapping("allrole")
    public @ResponseBody Map<String,Object> allrole(int page,int limit){

        List<Role> roleList = permissionService.getAllRole();
        int count = roleList.size();
        PageHelper.startPage(page,limit);

        List<Role> roleList1 = permissionService.getAllRole();

        PageInfo<Role> rolePageInfo = new PageInfo<>(roleList1);

        List<Role> roleList2 = rolePageInfo.getList();

        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("code",0);
        resultMap.put("msg","");
        resultMap.put("count",count);


        resultMap.put("data",roleList2);
        return resultMap;

    }

    //根据roleId删除role表中的role
    @ResponseBody
    @RequestMapping("permission/deleterole/{roleId}")
    public void deleterole(@PathVariable("roleId") String roleId){
        permissionService.deleteRoleByroleId(roleId);
    }

}