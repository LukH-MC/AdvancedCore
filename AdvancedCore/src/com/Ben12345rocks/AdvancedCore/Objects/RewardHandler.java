package com.Ben12345rocks.AdvancedCore.Objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.bukkit.Bukkit;

import com.Ben12345rocks.AdvancedCore.AdvancedCoreHook;
import com.Ben12345rocks.AdvancedCore.Exceptions.FileDirectoryException;
import com.Ben12345rocks.AdvancedCore.UserManager.UserManager;
import com.Ben12345rocks.AdvancedCore.Util.Misc.ArrayUtils;

/**
 * The Class RewardHandler.
 */
public class RewardHandler {

	/** The instance. */
	static RewardHandler instance = new RewardHandler();

	/** The plugin. */
	AdvancedCoreHook plugin = AdvancedCoreHook.getInstance();

	/**
	 * Gets the single instance of RewardHandler.
	 *
	 * @return single instance of RewardHandler
	 */
	public static RewardHandler getInstance() {
		return instance;
	}

	/** The rewards. */
	private ArrayList<Reward> rewards;

	/**
	 * Instantiates a new reward handler.
	 */
	private RewardHandler() {
		rewardFolders = new ArrayList<File>();
		setDefaultFolder(new File(AdvancedCoreHook.getInstance().getPlugin().getDataFolder(), "Rewards"));
	}

	/** The default folder. */
	private File defaultFolder;

	/** The reward folders. */
	private ArrayList<File> rewardFolders;

	/**
	 * Adds the reward folder.
	 *
	 * @param file
	 *            the file
	 */
	public synchronized void addRewardFolder(File file) {
		file.mkdirs();
		if (file.isDirectory()) {
			if (!rewardFolders.contains(file)) {
				rewardFolders.add(file);
				loadRewards();
			}
		} else {
			plugin.debug(file.getAbsolutePath());
			try {
				throw new FileDirectoryException("File is not a directory");
			} catch (FileDirectoryException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check delayed timed rewards.
	 */
	public synchronized void checkDelayedTimedRewards() {
		com.Ben12345rocks.AdvancedCore.Thread.Thread.getInstance().run(new Runnable() {

			@Override
			public void run() {
				for (String uuid : UserManager.getInstance().getAllUUIDs()) {
					User user = UserManager.getInstance().getUser(new UUID(uuid));
					for (Reward reward : getRewards()) {
						ArrayList<Long> times = user.getTimedReward(reward);
						for (Long t : times) {
							long time = t.longValue();
							if (time != 0) {
								Date timeDate = new Date(time);
								if (new Date().after(timeDate)) {
									reward.giveRewardReward(user, true);
									user.removeTimedReward(reward, time);
								}
							}
						}
					}
				}
			}
		});

	}

	/**
	 * Gets the reward.
	 *
	 * @param reward
	 *            the reward
	 * @return the reward
	 */
	public synchronized Reward getReward(String reward) {
		reward = reward.replace(" ", "_");

		for (Reward rewardFile : getRewards()) {
			if (rewardFile.name.equalsIgnoreCase(reward)) {
				return rewardFile;
			}
		}

		if (reward.equals("")) {
			plugin.getPlugin().getLogger().warning("Tried to get any empty reward file name, renaming to EmptyName");
			reward = "EmptyName";
		}

		return new Reward(defaultFolder, reward);
	}

	/**
	 * Gets the rewards.
	 *
	 * @return the rewards
	 */
	public synchronized ArrayList<Reward> getRewards() {
		if (rewards == null) {
			rewards = new ArrayList<Reward>();
		}
		return rewards;
	}

	/**
	 * Give a user reward file
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 */
	public synchronized void giveReward(User user, Reward reward) {
		giveReward(user, reward, user.isOnline());
	}

	/**
	 * Give a user multiple rewards
	 * 
	 * @param user
	 *            the user
	 * @param rewards
	 *            rewards
	 */
	public synchronized void giveReward(User user, Reward... rewards) {
		for (Reward reward : rewards) {
			giveReward(user, reward);
		}
	}

	/**
	 * Give a user multiple rewards
	 * 
	 * @param user
	 *            the user
	 * @param rewards
	 *            rewards
	 */
	public synchronized void giveReward(User user, String... rewards) {
		for (String reward : rewards) {
			giveReward(user, reward);
		}
	}

	public synchronized void giveReward(User user, boolean online, String... rewards) {
		for (String reward : rewards) {
			giveReward(user, reward, online);
		}
	}

	/**
	 * Give reward
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 * @param online
	 *            the online
	 */
	public synchronized void giveReward(User user, Reward reward, boolean online) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				reward.giveReward(user, online);
			}
		});
		
	}

	/**
	 * Give reward.
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 */
	public synchronized void giveReward(User user, String reward) {
		if (!reward.equals("")) {
			giveReward(user, getReward(reward), user.isOnline());
		}
	}

	/**
	 * Give reward.
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 * @param online
	 *            the online
	 */
	public synchronized void giveReward(User user, String reward, boolean online) {
		if (!reward.equals("")) {
			giveReward(user, getReward(reward), online);
		}
	}

	/**
	 * Give reward.
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 * @param online
	 *            the online
	 * @param giveOffline
	 *            the give offline
	 */
	public synchronized void giveReward(User user, Reward reward, boolean online, boolean giveOffline) {
		reward.giveReward(user, online, giveOffline);
	}

	/**
	 * Give reward.
	 *
	 * @param user
	 *            the user
	 * @param reward
	 *            the reward
	 * @param online
	 *            the online
	 * @param giveOffline
	 *            the give offline
	 */
	public synchronized void giveReward(User user, String reward, boolean online, boolean giveOffline) {
		if (!reward.equals("")) {
			giveReward(user, getReward(reward), online, giveOffline);
		}
	}

	/**
	 * Load rewards.
	 */
	public synchronized void loadRewards() {
		rewards = new ArrayList<Reward>();
		setupExample();
		for (File file : rewardFolders) {
			for (String reward : getRewardNames(file)) {
				if (!reward.equals("")) {
					if (!rewardExist(reward)) {
						rewards.add(new Reward(file, reward));
						plugin.debug("Loaded Reward File: " + file.getAbsolutePath() + "/" + reward);
					} else {
						plugin.getPlugin().getLogger().warning("Detected that a reward file named " + reward
								+ " already exists, cannot load reward file " + file.getAbsolutePath() + "/" + reward);
					}
				} else {
					plugin.getPlugin().getLogger().warning(
							"Detected getting a reward file with an empty name! That means you either didn't type a name or didn't properly make an empty list");
				}
			}
		}
		plugin.debug("Loaded rewards");

	}

	/**
	 * Copy file.
	 *
	 * @param fileName
	 *            the file name
	 */
	private void copyFile(String fileName) {
		File file = new File(plugin.getPlugin().getDataFolder(), "Rewards" + File.separator + fileName);
		if (!file.exists()) {
			plugin.getPlugin().saveResource("Rewards" + File.separator + fileName, true);
		}
	}

	/**
	 * Setup example.
	 */
	public void setupExample() {
		if (!plugin.getPlugin().getDataFolder().exists()) {
			plugin.getPlugin().getDataFolder().mkdir();
		}

		copyFile("ExampleBasic.yml");
		copyFile("ExampleAdvanced.yml");
	}

	/**
	 * Reward exist.
	 *
	 * @param reward
	 *            the reward
	 * @return true, if successful
	 */
	public synchronized boolean rewardExist(String reward) {
		if (reward.equals("")) {
			return false;
		}
		for (Reward rewardName : getRewards()) {
			if (rewardName.getRewardName().equalsIgnoreCase(reward)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the reward files.
	 *
	 * @param folder
	 *            the folder
	 * @return the reward files
	 */
	public synchronized ArrayList<String> getRewardFiles(File folder) {
		String[] fileNames = folder.list();
		return ArrayUtils.getInstance().convert(fileNames);
	}

	/**
	 * Gets the reward names.
	 *
	 * @param file
	 *            the file
	 * @return the reward names
	 */
	public synchronized ArrayList<String> getRewardNames(File file) {
		ArrayList<String> rewardFiles = getRewardFiles(file);
		if (rewardFiles == null) {
			return new ArrayList<String>();
		}
		for (int i = 0; i < rewardFiles.size(); i++) {
			rewardFiles.set(i, rewardFiles.get(i).replace(".yml", ""));
		}

		Collections.sort(rewardFiles, String.CASE_INSENSITIVE_ORDER);

		return rewardFiles;
	}

	/**
	 * Gets the default folder.
	 *
	 * @return the default folder
	 */
	public synchronized File getDefaultFolder() {
		return defaultFolder;
	}

	/**
	 * Sets the default folder.
	 *
	 * @param defaultFolder
	 *            the new default folder
	 */
	public synchronized void setDefaultFolder(File defaultFolder) {
		this.defaultFolder = defaultFolder;
	}
}
