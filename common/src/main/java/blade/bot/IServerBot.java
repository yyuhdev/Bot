package blade.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public interface IServerBot {
    String PROFILE_NAME = "$Bot";

    Property[] SKINS = new Property[] {
            new Property("textures", // https://mineskin.org/358793428
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyNjgzOTA5NzI2OCwKICAicHJvZmlsZUlkIiA6ICIxNmJhNWU4MDJhMmU0ZDJhYjEwZmZiYWJiYmQ1MDdlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzbGlua3l1c2VyMzMxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDNmMDViNjY1NzY3NGQ0MThiZDQ0ZDA3YTM3ZjAyMzliYmVhYjJmMWFkYzIwZGE1ODA5YWE4MjMxMGQzYzE5IgogICAgfQogIH0KfQ==",
                    "HzRUC1ohxxDuZ/gSemRU/nvQsknmmiZ6pvud5fMvYQTylJMPfKun6NB2RcIXbl4daRxSnDL/HikAMF4EziHM73HZmOcpxHg0YI/W2HDfd+Mlc6HeBSXlL8DA23C7LSfFP8ZlVE67jAIbjfZZ+auqNZ9OyGHPvXYY878HV8JGHHBtuyFMFvwbSsHDjBmtgQ4hse9Kypg5Pzhz4F7fKWUAnDBkxiduQp5fc4WL4nNb7AHLGUmp/v8OS14SLOzO1lE4InFbeyzybQfAPhjWGW0kg/CwtSwLV9B3iUXnNzrBw8A1zGpBQMKBnkrp6L40kQ99pFSb1BWb6g5VuqdbjvdZIMamrhyP49rdk7K52eGESUkKPTebXJdT6oT1D6p8W3ptPNhqTLU6Hb9QJfI3Kp4Pfs3xMtVAgMne7kmynpOFRHELcYz2WJt4uBXGJfLwOzUFIskFm65139EoP6e90HMtIJt97IT1bMOqYbOCmKI04VfjFY8NFfeNSMn7sHv+BuOMegNhq4iRUSbzepUZzp57HkYoMYe9Rt+epwFhQC06IvzZV3hm0/YkkW5vf4lvP/wLOvNQoH15phGCws+21Yq2G+K6iykZnGyYztsDiB+D8a+9i7s9MqHiCXtvc7Jpjin0lmi/fo5/YMgnca3y5BVgP+o5eFJYK9pBK+lQGNJXuWQ="),
            new Property("textures", // https://mineskin.org/728604141
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyNjgzODk1NzMyNiwKICAicHJvZmlsZUlkIiA6ICIxYjQwYzcxMGZjMTY0NmQ2OTIxOTVmYzY3YzZlMTE0ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3c3pvbHNvbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNWVlMzNhNDk4ZjI4MDMxOGFiNWM4YzBjZDFiMWFjMDI1NDMwOTMzYzdkOTdlZDRiYTg3YTBhMDNhN2E0ZWYxIgogICAgfQogIH0KfQ==",
                    "lUbDqYS3pF3eQh/TDZklyem9b+G0F8dMjDK5bwjH+LgjCvDXWM4Buq56CyGX9h2QoqlT1eBVs/QTPaQJAU7wI9C4GWIO8rU19rqbvu5fu59D6dO9aQNS4t2oEPLd9tKyCH8o+Cc817u26Z1NGCydRomQABhME+kgdG9HGNcbfCcX+xkM0MEw3oh/pnzHjlJGz+6iVbrnp9hrASgTUo1FSJ+Jlu2PL0dHv8q6op9mHuLp3aPKznos7FNtFedHxZWTJ26Mn6F8ZwOAHGElBeAmGFL3XiMemSzYHiIxEEpsZd8Ik2mC3m95HgIWfbdEyGPB27PFppZVILNNx3Qqe+qtO/bmss9wKF7Nwg2F2c5sAxK1TVqnSaKhSenOIIoEOgu3E6sMYecgFzTHMl0HbcBSxSYHvSoYKVdphJ8vqMvSVpk8hFwOlLLzSXK/9csGJlQ/eEBbugekpM/NIJVwHorHqMNx3awfBaIU9yvVLSZ9UY6GfVgaAcUz6H+G+w/nlahbh0hYpCo8COBjJCq1Jsk+oEMqHnKeCUuGczcysugyDQ6llvdj90PRVOM65TNns1fiBZE+y2R8GMzXjDKIquLFTbhKXNUaCcvii5JLduX6x2+XEk23zZaPKVxsqNtsMMZzwRFA5E89d+SGi44A0pKC8usnIJzdKrMgnXxum5ye+JM="),
            new Property("textures", // https://mineskin.org/1190145668
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxMTM2ODI4NDUzNiwKICAicHJvZmlsZUlkIiA6ICJlNzkzYjJjYTdhMmY0MTI2YTA5ODA5MmQ3Yzk5NDE3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVfSG9zdGVyX01hbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81Y2Y5NTFkNWYyM2U3ODE0YjM0ZjFjZWY4ODQ0Nzc2MzRiNjcyNWFhYjAzNGIxZDIyYWVkMmY3N2MzMjQzNjcwIgogICAgfQogIH0KfQ==",
                    "Pdqi2hCM7HrUL8a6Y/4w70rHTrYKY15qPiVaTDSOE8IrHYlH62Vglh4pj4pEIFuKgObRXGhQJXtufkOA7C7HCn/YYNbUOTbJ4UBkfCoLwNdNWKkz3YVw3BZm3x0Mjmbkt63grMHRKIIhL0oedl/N5cVVdURzG6dasoT9EzJF/KxooJDYDj+fotsW1XA8GbihBNHMnksp77XFT0JKh/aws54RXYZ/3rpWLxmIX2YqcUlRNFMcXeDed4VsFBD4aQTxLHFjvJ1XTrx/v4pJJWd9ksGw9z+VEnnp0cJq8RSV7/b1aEPC6b0/89yK2WZsvoMW1vu1OygZC5X5n9eemg0vt/V1zMoTAXuwnlICKvPysR3D2ZjifHUJf8kOBL44B6u0L2ovR0IueVtk1O+ZEu3PsGvcJvv3V0bDfv31fUKmZ5qTR9YtfNK+uS+vIFhYR+beOLNyScAkVyLvvSrBLDyxWOcTasLKZUlNEMvIT1ma6tOg0n9VxDGk4dhReUDfG/sPAW+1mTi83hbT062SDIYwG/wiz8/79cvhOOjOOwxfVc0xkvhkRABVk5LRe0igTyl7drpWfbhs2yKZ65CsbxAt1OtNVSEhDo7LYx+qFWUvuReWqaRbdPFYAnh9WCcbPM++m/EBbpRZfUoblIJE8lxX7JAX9T+XCFruE7QOdzTQqxg="),
            new Property("textures", // https://minesk.in/1814657815
                    "ewogICJ0aW1lc3RhbXAiIDogMTY0MTM0Njg0NzIxOSwKICAicHJvZmlsZUlkIiA6ICIzM2ViZDMyYmIzMzk0YWQ5YWM2NzBjOTZjNTQ5YmE3ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYW5ub0JhbmFubm9YRCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mNGFkYTNiZGY4YzNmZTU0MGM4ZmI4NDQ0NTNjMjEzNjZiZWExMTc3YTJhM2NjMDcxYmEzNjBhMDAzMjNiNTlmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                    "XxjO34Y0qCXKc+3isU4x/0s1koZawiX8raIQa+WyGzXov2p8tSwt1gkFeY2qgedr1wokaWqEKRGFuYKJVDhYzBKsOSAYz7N89KpyzMaRusWCJsFso3vnSNJIirX2iLjnRNHiN6gssDslbM4FAMdfknJh55DtFKOQxeehJD+7SdaIkHoZmmtXdwZ8bkl0kj9/Xt1sHI621gZz/22vpDwpnlrOS41hyOG8AM9qBlDXUz8Xw1xvX1MUl3dr6288gL6Z/aiXa3jYVBEiygrXP7JkGzkSE9cOAivRy97GrcL8OZtiuDvZ4TDQbF7Lh3isKO1eiC29FBsxAQ/BiGQA8tr8L/jMtkA2MWiIQPMXT4zcCorWtCi3strBuaTeur0UBO36BC6T5JmW6xwgqzQCiCCODY8G+0vDqJtsurr9yh4zQTXCcmWNZFXuGNwi16AR9rfdX48irg5A8OEEYHEkkOLcBrxcWfsfEDT2SqnnfYp5W3PlNHbdkRwY9v/bmBncHFLe11PpHxpXIjBaw9LFkTAxQrUFc1wHObaOjUozAvTFlHqudLfaLuSi9gIHDSsGMlg7Cb6b6cJGMpg2xO6PKDftr98ErOx5MHErlCvzvuyi9lFXzSNp34Nwib6XgtDhnOBvaGww9eoL1odyB9u/p9TsyFchiawpxXWzqoNk9Hhtus4="),
    };

    ServerPlayer getSpawner();

    ServerBotSettings getSettings();

    ServerPlayer getVanillaPlayer();

    void destroy();

    static GameProfile getProfile() {
        GameProfile profile = new GameProfile(UUID.randomUUID(), PROFILE_NAME);
        PropertyMap properties = profile.getProperties();
        properties.put("textures", SKINS[ThreadLocalRandom.current().nextInt(SKINS.length)]);
        return profile;
    }
}
