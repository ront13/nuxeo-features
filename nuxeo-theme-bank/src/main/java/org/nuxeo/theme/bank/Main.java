/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.theme.bank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.theme.presets.PaletteIdentifyException;
import org.nuxeo.theme.presets.PaletteParseException;
import org.nuxeo.theme.presets.PaletteParser;
import org.nuxeo.theme.resources.BankManager;
import org.nuxeo.theme.resources.BankUtils;

@WebObject(type = "theme-banks")
@Produces(MediaType.TEXT_HTML)
public class Main extends ModuleRoot {

    private static final Log log = LogFactory.getLog(Main.class);

    private static final String SERVER_ID = "Nuxeo/ThemeBank-1.0";

    @GET
    public Object getIndex() {
        return getTemplate("index.ftl");
    }

    /*
     * Management mode
     */
    @Path("{bank}/manage")
    public Object getManagement(@PathParam("bank") String bank) {
        return newObject("Management", bank);
    }

    /*
     * Banks
     */
    @GET
    @Path("{bank}")
    public Object displayBank(@PathParam("bank") String bank) {
        return getTemplate("index.ftl").arg("bank", bank);
    }

    @GET
    @Path("{bank}/view")
    public Object displayBankView(@PathParam("bank") String bank) {
        return getTemplate("bank.ftl").arg("bank", bank);
    }

    @GET
    @Path("{bank}/info")
    public Object getBankInfo(@PathParam("bank") String bank) {
        return "XXX";
    }

    @GET
    @Path("{bank}/logo")
    public Object displayBankLogo(@PathParam("bank") String bank) {
        File file;
        try {
            file = BankManager.getBankLogoFile(bank);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        if (file == null || !file.exists()) {
            return noPreview();
        }
        String ext = FileUtils.getFileExtension(path);
        String mimeType = ctx.getEngine().getMimeType(ext);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return Response.ok().entity(Utils.streamFile(file)).lastModified(
                new Date(file.lastModified())).header("Cache-Control", "public").header(
                "Server", SERVER_ID).type(mimeType).build();
    }

    /*
     * UI
     */
    @GET
    @Path("navtree")
    public Object getNavtreeView() {
        return getTemplate("navtree.ftl");
    }

    @GET
    @Path("actionbar")
    public Object getActionBarView() {
        return getTemplate("actionbar.ftl");
    }

    @GET
    @Path("banks")
    public Object getBanksView() {
        return getTemplate("banks.ftl");
    }

    @GET
    @Path("session/login")
    public Object getSessionView() {
        return getTemplate("session.ftl");
    }

    @GET
    @Path("session")
    public Object doSession() {
        Object failed = ctx.getProperty("failed");
        if (failed == null) {
            return getIndex();
        }
        return getSessionView();
    }

    @POST
    @Path("session/@@login")
    public Object login() {
        return getIndex();
    }

    /*
     * Styles
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{bank}/json/skins")
    @SuppressWarnings("rawtypes")
    public String listBankSkins(@PathParam("bank") String bankName) {
        JSONArray skins = new JSONArray();
        try {
            for (String collection : BankManager.getCollections(bankName)) {
                Map<String, Object> info = BankManager.getInfo(bankName,
                        collection, "style");
                if (info == null) {
                    continue;
                }
                for (Map.Entry<String, Object> entry : info.entrySet()) {
                    String resource = entry.getKey();
                    Map value = (Map) entry.getValue();
                    Boolean isSkin = false;
                    if (value.containsKey("skin")) {
                        isSkin = (Boolean) value.get("skin");
                    }
                    if (isSkin) {
                        JSONObject skinMap = new JSONObject();
                        skinMap.put("bank", bankName);
                        skinMap.put("collection", collection);
                        skinMap.put("resource", resource);
                        skinMap.put("name", String.format("%s (%s)",
                                resource.replace(".css", ""), collection));
                        skins.add(skinMap);
                    }
                }
            }
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        return skins.toString();
    }

    public List<String> listSkinsInCollection(String bankName, String collection) {
        Map<String, Object> info;
        try {
            info = BankManager.getInfo(bankName, collection, "style");
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        List<String> skins = new ArrayList<String>();
        if (info != null) {
            for (Map.Entry<String, Object> entry : info.entrySet()) {
                String resource = entry.getKey();
                Map value = (Map) entry.getValue();
                Boolean isSkin = false;
                if (value.containsKey("skin")) {
                    isSkin = (Boolean) value.get("skin");
                }
                if (isSkin) {
                    skins.add(resource);
                }
            }
        }
        return skins;
    }

    @GET
    @Path("{bank}/{collection}/view")
    public Object getCollectionView(@PathParam("bank") String bank,
            @PathParam("collection") String collection) {
        return getTemplate("collection.ftl").arg("collection", collection).arg(
                "bank", bank);
    }

    @GET
    @Path("{bank}/{collection}/{type}/info")
    public Object getPresetCollectionInfo(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("type") String typeName) {
        try {
            return BankManager.getInfoFile(bank, collection, typeName);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
    }

    @GET
    @Path("{bank}/{collection}/style/view")
    public Object getStyleCollectionsView(@PathParam("bank") String bank,
            @PathParam("collection") String collection) {
        return getStyleCollections(bank, collection, false);
    }

    @GET
    @Path("{bank}/{collection}/skin/view")
    public Object getSkinsCollectionsView(@PathParam("bank") String bank,
            @PathParam("collection") String collection) {
        return getStyleCollections(bank, collection, true);
    }

    public Object getStyleCollections(String bank, String collection,
            Boolean skins_only) {
        return getTemplate("styleCollection.ftl").arg("styles",
                Utils.getItemsInCollection(bank, collection, "style")).arg(
                "skins", listSkinsInCollection(bank, collection)).arg(
                "collection", collection).arg("bank", bank).arg("skins_only",
                skins_only);
    }

    @GET
    @Produces("text/css")
    @Path("{bank}/{collection}/style/{resource}")
    public Response getStyle(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource) {
        File file;
        try {
            file = BankManager.getStyleFile(bank, collection, resource);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        return Response.ok().entity(Utils.streamFile(file)).lastModified(
                new Date(file.lastModified())).header("Cache-Control", "public").header(
                "Server", SERVER_ID).build();
    }

    @GET
    @Path("{bank}/{collection}/style/{resource}/{action}")
    public Object renderStyle(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource,
            @PathParam("action") String action) {
        return getTemplate("style.ftl").arg("content",
                getStyleContent(bank, collection, resource)).arg("bank", bank).arg(
                "resource", resource).arg("collection", collection).arg(
                "action", action).arg("is_skin", true);
    }

    @GET
    @Path("{bank}/{collection}/skin/{resource}/{action}")
    public Object renderSkin(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource,
            @PathParam("action") String action) {
        return getTemplate("style.ftl").arg("content",
                getStyleContent(bank, collection, resource)).arg("bank", bank).arg(
                "resource", resource).arg("collection", collection).arg(
                "action", action).arg("is_skin", true);
    }

    @GET
    @Path("{bank}/{collection}/style/{resource}/preview")
    public Object displayStylePreview(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource) {
        File file;
        try {
            file = BankManager.getStylePreviewFile(bank, collection, resource);
        } catch (IOException e) {
            return noPreview();
        }
        String ext = FileUtils.getFileExtension(path);
        String mimeType = ctx.getEngine().getMimeType(ext);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return Response.ok().entity(Utils.streamFile(file)).lastModified(
                new Date(file.lastModified())).header("Cache-Control", "public").header(
                "Server", SERVER_ID).type(mimeType).build();
    }

    public String getStyleContent(String bank, String collection,
            String resource) {
        File file;
        try {
            file = BankManager.getStyleFile(bank, collection, resource);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        String content;
        try {
            content = BankUtils.getFileContent(file);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        return content;
    }

    /*
     * Presets
     */

    @GET
    @Path("{bank}/{collection}/preset/view")
    public Object getPresetCollectionView(@PathParam("bank") String bank,
            @PathParam("collection") String collection) {
        return getTemplate("presetCollection.ftl").arg("presets",
                Utils.getItemsInCollection(bank, collection, "preset")).arg(
                "collection", collection).arg("bank", bank);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{bank}/{collection}/preset/{category}")
    public Response getPreset(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("category") String category) {
        String path = String.format("%s/%s/preset/%s", bank, collection,
                category);
        File file;
        try {
            file = BankManager.getFile(path);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        String content = "";
        if (!file.exists()) {
            return Response.status(404).build();
        }

        StringBuilder sb = new StringBuilder();
        for (File f : file.listFiles()) {
            try {
                content = BankUtils.getFileContent(f);
            } catch (IOException e) {
                log.warn("Could not read file: " + f.getAbsolutePath());
                continue;
            }
            content = PaletteParser.renderPaletteAsCsv(content.getBytes(),
                    f.getName());
            sb.append(content);
        }
        content = sb.toString();
        return Response.ok(content).lastModified(new Date(file.lastModified())).header(
                "Cache-Control", "public").header("Server", SERVER_ID).build();
    }

    @GET
    @Path("{bank}/{collection}/preset/{category}/view")
    public Object getPresetView(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("category") String category) {
        String path = String.format("%s/%s/preset/%s", bank, collection,
                category);
        File file;
        try {
            file = BankManager.getFile(path);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        Properties properties = new Properties();
        for (File f : file.listFiles()) {
            String content;
            try {
                content = BankUtils.getFileContent(f);
            } catch (IOException e) {
                log.warn("Could not read file: " + f.getAbsolutePath());
                continue;
            }
            try {
                properties.putAll(PaletteParser.parse(content.getBytes(),
                        f.getName()));
            } catch (PaletteIdentifyException e) {
                log.warn("Could not identify palette type: "
                        + f.getAbsolutePath());
            } catch (PaletteParseException e) {
                log.warn("Could not parse palette: " + f.getAbsolutePath());
            }
        }
        return getTemplate("preset.ftl").arg("properties", properties).arg(
                "bank", bank).arg("collection", collection).arg("category",
                category);
    }

    /*
     * Images
     */

    @GET
    @Path("{bank}/{collection}/image/view")
    public Object getImageCollectionView(@PathParam("bank") String bank,
            @PathParam("collection") String collection) {
        return getTemplate("imageCollection.ftl").arg("images",
                Utils.getItemsInCollection(bank, collection, "image")).arg(
                "collection", collection).arg("bank", bank);
    }

    @GET
    @Path("{bank}/{collection}/image/{resource}")
    public Response getImage(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource) {
        File file;
        try {
            file = BankManager.getImageFile(bank, collection, resource);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
        String ext = FileUtils.getFileExtension(path);
        String mimeType = ctx.getEngine().getMimeType(ext);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return Response.ok().entity(Utils.streamFile(file)).lastModified(
                new Date(file.lastModified())).header("Cache-Control", "public").header(
                "Server", SERVER_ID).type(mimeType).build();
    }

    @GET
    @Path("{bank}/{collection}/image/{resource}/view")
    public Object getImageView(@PathParam("bank") String bank,
            @PathParam("collection") String collection,
            @PathParam("resource") String resource) {
        String path = String.format("%s/%s/image/%s", bank, collection,
                resource);
        return getTemplate("image.ftl").arg("path", path).arg("bank", bank).arg(
                "resource", resource).arg("collection", collection);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{bank}/json/images")
    public String listImages(@PathParam("bank") String bank) {
        try {
            return Utils.listImages(bank);
        } catch (IOException e) {
            throw new ThemeBankException(e.getMessage(), e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("json/tree")
    public String getTree() throws IOException {
        return Utils.getNavTree();
    }

    private Object noPreview() {
        return redirect(ctx.getModulePath() + "/skin/img/no-preview.png");
    }

}